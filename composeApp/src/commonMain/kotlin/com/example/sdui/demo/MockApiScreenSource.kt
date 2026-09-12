package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.coroutines.cancellation.CancellationException

/**
 * Reference remote source for the Home document hosted by Mocki.
 *
 * The endpoint must return either a versioned [com.example.sdui.shared.SduiDocument]
 * or a legacy bare [UiNode] JSON payload. A bundled document remains the fallback
 * whenever remote delivery is unavailable or invalid.
 */
class MockApiScreenSource(
    private val localSource: ScreenSource = LocalDemoScreenSource(),
    private val httpClient: HttpClient = HttpClient(),
    private val fetchHomePayload: suspend () -> String = {
        httpClient.get(HOME_SCREEN_URL).bodyAsText()
    }
) : ScreenSource {
    override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode =
        when (val result = loadScreen(ScreenRequest(path, forceRefresh))) {
            is ScreenLoadResult.Success -> result.screen
            is ScreenLoadResult.Failure -> throw result.cause
        }

    override suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult {
        if (request.path != HOME_SCREEN_PATH) {
            return localSource.loadScreen(request)
        }

        return try {
            ScreenLoadResult.Success(
                screen = SduiDocumentCodec.decode(fetchHomePayload()).root,
                source = ScreenLoadSource.NETWORK
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            localSource.loadScreen(request)
        }
    }

    override fun prefetch(path: String) {
        if (path != HOME_SCREEN_PATH) localSource.prefetch(path)
    }

    override fun cancelPrefetch(path: String) {
        localSource.cancelPrefetch(path)
    }

    override fun close() {
        localSource.close()
        httpClient.close()
    }

    private companion object {
        const val HOME_SCREEN_PATH = "home"
        const val HOME_SCREEN_URL =
            "https://mocki.io/v1/491022fe-aff8-4a6e-9768-8220a1f97894"
    }
}
