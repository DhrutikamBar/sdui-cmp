package com.example.sdui.demo

import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockApiScreenSourceTest {
    @Test
    fun usesTheRemoteHomeDocumentWhenItIsValid() = runBlocking {
        val source = MockApiScreenSource(
            fetchHomePayload = { """{"schemaVersion":1,"root":{"type":"text"}}""" }
        )

        val result = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "home"))
        )

        assertEquals(ScreenLoadSource.NETWORK, result.source)
        assertEquals("text", result.screen.type)
        source.close()
    }

    @Test
    fun fallsBackToTheBundledHomeDocumentWhenRemoteLoadingFails() = runBlocking {
        val source = MockApiScreenSource(
            fetchHomePayload = { error("Mock endpoint unavailable") }
        )

        val result = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "home"))
        )

        assertEquals(ScreenLoadSource.UNKNOWN, result.source)
        assertEquals("column", result.screen.type)
        source.close()
    }

    @Test
    fun retainsBundledSourcesForNonRemotePaths() = runBlocking {
        val source = MockApiScreenSource(
            fetchHomePayload = { error("This request must not be made") }
        )

        val result = assertIs<ScreenLoadResult.Success>(
            source.loadScreen(ScreenRequest(path = "welcome"))
        )

        assertEquals("column", result.screen.type)
        source.close()
    }
}
