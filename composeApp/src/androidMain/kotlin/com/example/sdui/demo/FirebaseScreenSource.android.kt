package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.shared.UiNode
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

actual fun createFirebaseScreenSource(): ScreenSource = FirebaseFirestoreScreenSource()

/**
 * Android reference source for documents stored in the sduiScreens collection.
 *
 * Document IDs are routes. The content field holds the SDUI document. Revision
 * and updatedAt are optional metadata fields used for authoring and diagnostics.
 */
class FirebaseFirestoreScreenSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) : ScreenSource {
    private val memory = mutableMapOf<String, CachedScreen>()

    override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode =
        when (val result = loadScreen(ScreenRequest(path, forceRefresh))) {
            is ScreenLoadResult.Success -> result.screen
            is ScreenLoadResult.Failure -> throw result.cause
        }

    override suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult {
        val now = nowMillis()
        if (!request.forceRefresh) {
            memory[request.path]?.takeIf { now - it.cachedAtMillis < MEMORY_FRESHNESS_MILLIS }?.let {
                return ScreenLoadResult.Success(it.screen, ScreenLoadSource.MEMORY)
            }
        }

        return try {
            val snapshot = withTimeoutOrNull(REQUEST_TIMEOUT_MILLIS) {
                firestore.collection(SCREENS_COLLECTION)
                    .document(request.path)
                    .get()
                    .await()
            } ?: throw IllegalStateException(
                "Firestore request timed out after " + REQUEST_TIMEOUT_MILLIS + "ms"
            )

            if (!snapshot.exists()) {
                throw NoSuchElementException("Firestore SDUI screen not found: " + request.path)
            }

            val content = snapshot.get(CONTENT_FIELD)
                ?: throw IllegalStateException(
                    "Firestore SDUI screen is missing the " + CONTENT_FIELD + " field"
                )
            val cached = CachedScreen(
                screen = FirestoreScreenContentCodec.decode(content),
                revision = snapshot.getLong(REVISION_FIELD),
                updatedAt = snapshot.getString(UPDATED_AT_FIELD),
                cachedAtMillis = nowMillis()
            )
            memory[request.path] = cached
            println(
                "SDUI: Firestore screen loaded for " + request.path +
                    ", revision=" + (cached.revision ?: "unspecified") +
                    ", updatedAt=" + (cached.updatedAt ?: "unspecified")
            )
            ScreenLoadResult.Success(
                screen = cached.screen,
                source = if (snapshot.metadata.isFromCache) {
                    ScreenLoadSource.DISK
                } else {
                    ScreenLoadSource.NETWORK
                }
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Throwable) {
            println(
                "SDUI: Firestore screen load failed for " + request.path + ": " + cause.message
            )
            ScreenLoadResult.Failure(cause)
        }
    }

    override fun prefetch(path: String) = Unit

    override fun close() {
        memory.clear()
    }

    private data class CachedScreen(
        val screen: UiNode,
        val revision: Long?,
        val updatedAt: String?,
        val cachedAtMillis: Long
    )

    private companion object {
        const val SCREENS_COLLECTION = "sduiScreens"
        const val CONTENT_FIELD = "content"
        const val REVISION_FIELD = "revision"
        const val UPDATED_AT_FIELD = "updatedAt"
        const val REQUEST_TIMEOUT_MILLIS = 10_000L
        const val MEMORY_FRESHNESS_MILLIS = 5 * 60 * 1000L
    }
}
