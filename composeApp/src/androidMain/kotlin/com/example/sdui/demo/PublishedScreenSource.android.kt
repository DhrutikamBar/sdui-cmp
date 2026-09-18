package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

private const val PUBLISHED_API = "https://sdui-studio--sdui-cmp.us-east4.hosted.app/api/published-screens"

actual fun createPublishedScreenSource(): ScreenSource = PublishedScreenSource()

/** Loads only releases exposed by the Studio published-screen endpoint. */
class PublishedScreenSource(
    private val baseUrl: String = PUBLISHED_API,
    private val client: HttpClient = HttpClient(OkHttp),
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
            require(ROUTE_PATTERN.matches(request.path)) { "Invalid screen route: ${request.path}" }
            val response = withTimeoutOrNull(REQUEST_TIMEOUT_MILLIS) {
                client.get("${baseUrl.trimEnd('/')}/${request.path}")
            } ?: throw IllegalStateException("Published screen request timed out")
            if (response.status.value == 404) throw NoSuchElementException("No published screen: ${request.path}")
            if (response.status.value !in 200..299) {
                throw IllegalStateException("Published screen request failed (${response.status.value})")
            }
            val payload = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val document = payload["document"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("Published screen response has no document")
            val screen = SduiDocumentCodec.decode(document).root
            memory[request.path] = CachedScreen(screen, nowMillis())
            ScreenLoadResult.Success(screen, ScreenLoadSource.NETWORK)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Throwable) {
            println("Published screen load failed for ${request.path}: ${cause.message}")
            ScreenLoadResult.Failure(cause)
        }
    }

    override fun prefetch(path: String) = Unit

    override fun close() {
        memory.clear()
        client.close()
    }

    private data class CachedScreen(val screen: UiNode, val cachedAtMillis: Long)

    private companion object {
        val ROUTE_PATTERN = Regex("^[a-zA-Z0-9][a-zA-Z0-9-]{0,127}$")
        const val REQUEST_TIMEOUT_MILLIS = 10_000L
        const val MEMORY_FRESHNESS_MILLIS = 5 * 60 * 1000L
    }
}

