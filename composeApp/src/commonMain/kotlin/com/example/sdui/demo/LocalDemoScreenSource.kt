package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.app.LocalScreens
import com.example.sdui.app.decodeLocalScreen
import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import kotlin.coroutines.cancellation.CancellationException

/** Bundled, offline-only source for the reference application. */
class LocalDemoScreenSource : ScreenSource {
    private val memory = mutableMapOf<String, UiNode>()

    override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode =
        load(path, forceRefresh).screen

    override suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult =
        try {
            val result = load(request.path, request.forceRefresh)
            ScreenLoadResult.Success(result.screen, result.source)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (cause: Throwable) {
            ScreenLoadResult.Failure(cause)
        }

    override fun prefetch(path: String) {
        if (path !in memory) {
            runCatching { memory[path] = decode(path) }
        }
    }

    private fun load(path: String, forceRefresh: Boolean): LocalResult {
        if (!forceRefresh) {
            memory[path]?.let { return LocalResult(it, ScreenLoadSource.MEMORY) }
        }
        val screen = decode(path)
        memory[path] = screen
        return LocalResult(screen, ScreenLoadSource.UNKNOWN)
    }

    private fun decode(path: String): UiNode = SduiDocumentCodec.fromLegacy(
        decodeLocalScreen(
            when (path) {
            "home" -> LocalScreens.home
            "welcome" -> LocalScreens.welcome
            "wallet" -> LocalScreens.wallet
            "checkout" -> LocalScreens.checkout
                else -> throw NoSuchElementException("No bundled SDUI screen for path: $path")
            }
        )
    ).root

    private data class LocalResult(val screen: UiNode, val source: ScreenLoadSource)
}
