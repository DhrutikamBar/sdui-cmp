package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LocalDemoScreenSourceTest {
    @Test
    fun bundledScreenIsValidatedAndThenServedFromMemory() = runBlocking {
        val source = LocalDemoScreenSource()

        val first = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "home"))
        )
        val second = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "home"))
        )

        assertEquals(ScreenLoadSource.UNKNOWN, first.source)
        assertEquals(ScreenLoadSource.MEMORY, second.source)
        assertEquals(first.screen, second.screen)
    }

    @Test
    fun forceRefreshRevalidatesTheBundledScreen() = runBlocking {
        val source = LocalDemoScreenSource()
        source.loadScreen(ScreenRequest(path = "welcome"))

        val refreshed = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "welcome", forceRefresh = true))
        )

        assertEquals(ScreenLoadSource.UNKNOWN, refreshed.source)
    }

    @Test
    fun unknownBundledScreenIsReportedAsFailure() = runBlocking {
        val result = LocalDemoScreenSource().loadScreen(ScreenRequest(path = "missing"))

        val failure = assertIs<ScreenLoadResult.Failure>(result)
        assertEquals("No bundled SDUI screen for path: missing", failure.cause.message)
    }

    @Test
    fun loadsAllImportantBundledFallbackRoutes() = runBlocking {
        val source = LocalDemoScreenSource()

        listOf("send", "send-success", "settings", "order-confirmed", "lottie-test", "ui-certification").forEach { path ->
            val result = assertIs<ScreenLoadResult.Success>(
                source.loadScreen(ScreenRequest(path = path))
            )
            assertEquals("column", result.screen.type)
        }
    }

    @Test
    fun certificationFixtureContainsComplexLayoutAndFallbackCoverage() = runBlocking {
        val result = assertIs<ScreenLoadResult.Success>(
            LocalDemoScreenSource().loadScreen(ScreenRequest(path = "ui-certification"))
        )

        val types = mutableListOf<String>()
        fun visit(node: com.example.sdui.shared.UiNode) {
            types += node.type
            node.children.forEach(::visit)
            node.fallback?.let(::visit)
        }
        visit(result.screen)

        listOf("box", "grid", "list", "row", "switch", "futureWidget").forEach {
            kotlin.test.assertTrue(it in types, "Expected $it in certification fixture")
        }
    }
}
