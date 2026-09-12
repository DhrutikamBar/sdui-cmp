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
}
