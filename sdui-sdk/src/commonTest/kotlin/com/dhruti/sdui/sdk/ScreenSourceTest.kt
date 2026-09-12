package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiNode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ScreenSourceTest {
    @Test
    fun defaultLoadScreenWrapsASuccessfulLegacySource() = runBlocking {
        val screen = UiNode(type = "text")
        val source = StubScreenSource(screen = screen)

        val result = source.loadScreen(ScreenRequest(path = "home"))

        assertEquals(
            ScreenLoadResult.Success(screen, ScreenLoadSource.UNKNOWN),
            result
        )
    }

    @Test
    fun defaultLoadScreenWrapsAFailure() = runBlocking {
        val source = StubScreenSource(failure = IllegalStateException("Unavailable"))

        val result = source.loadScreen(ScreenRequest(path = "home"))

        val failure = assertIs<ScreenLoadResult.Failure>(result)
        assertEquals("Unavailable", failure.cause.message)
    }

    private class StubScreenSource(
        private val screen: UiNode? = null,
        private val failure: Throwable? = null
    ) : ScreenSource {
        override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode {
            failure?.let { throw it }
            return requireNotNull(screen)
        }

        override fun prefetch(path: String) = Unit
    }
}
