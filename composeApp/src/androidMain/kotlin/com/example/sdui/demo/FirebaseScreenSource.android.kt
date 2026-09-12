package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

actual fun createFirebaseScreenSource(): ScreenSource = FirebaseFirestoreScreenSource()

/**
 * Android reference source for documents stored in the sduiScreens collection.
 * Each document ID is an SDUI route and has a map field named content.
 */
class FirebaseFirestoreScreenSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ScreenSource {
    private val memory = mutableMapOf<String, UiNode>()

    override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode =
        when (val result = loadScreen(ScreenRequest(path, forceRefresh))) {
            is ScreenLoadResult.Success -> result.screen
            is ScreenLoadResult.Failure -> throw result.cause
        }

    override suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult {
        if (!request.forceRefresh) {
            memory[request.path]?.let {
                return ScreenLoadResult.Success(it, ScreenLoadSource.MEMORY)
            }
        }

        return try {
            val snapshot = firestore.collection(SCREENS_COLLECTION)
                .document(request.path)
                .get()
                .await()

            if (!snapshot.exists()) {
                throw NoSuchElementException("Firestore SDUI screen not found: " + request.path)
            }

            val content = snapshot.get(CONTENT_FIELD)
                ?: throw IllegalStateException(
                    "Firestore SDUI screen is missing the " + CONTENT_FIELD + " field"
                )
            val screen = SduiDocumentCodec.decode(content.toJsonElement()).root
            memory[request.path] = screen
            ScreenLoadResult.Success(
                screen = screen,
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

    private companion object {
        const val SCREENS_COLLECTION = "sduiScreens"
        const val CONTENT_FIELD = "content"
    }
}

private fun Any.toJsonElement(): JsonElement = when (this) {
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Int -> JsonPrimitive(this)
    is Long -> JsonPrimitive(this)
    is Float -> JsonPrimitive(this)
    is Double -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(
        entries.associate { (key, value) ->
            require(key is String) { "Firestore SDUI object keys must be strings" }
            key to (value?.toJsonElement() ?: JsonNull)
        }
    )
    is List<*> -> JsonArray(map { it?.toJsonElement() ?: JsonNull })
    else -> throw IllegalArgumentException(
        "Unsupported Firestore SDUI value: " + this::class.simpleName
    )
}
