package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionRegistryTest {
    @Test
    fun deniedActionDoesNotReachTheRegisteredHandler() {
        var invoked = false
        val registry = ActionRegistry(
            actionPolicy = SduiActionPolicy { SduiActionDecision.Deny("Blocked by host policy") }
        ).apply {
            register("apiCall") { invoked = true }
        }

        registry.dispatch(UiAction(type = "apiCall"))

        assertFalse(invoked)
    }

    @Test
    fun defaultPolicyPreservesExistingDispatchBehavior() {
        var invoked = false
        val registry = ActionRegistry().apply {
            register("navigate") { invoked = true }
        }

        registry.dispatch(UiAction(type = "navigate", target = "home"))

        assertTrue(invoked)
    }

    @Test
    fun interceptorsRunInOrderBeforeTheHandler() {
        val calls = mutableListOf<String>()
        val registry = ActionRegistry(
            interceptors = listOf(
                ActionInterceptor { _, next ->
                    calls += "first-before"
                    next(UiAction(type = "navigate", target = "welcome"))
                    calls += "first-after"
                },
                ActionInterceptor { _, next ->
                    calls += "second-before"
                    next(UiAction(type = "navigate", target = "wallet"))
                    calls += "second-after"
                }
            )
        ).apply {
            register("navigate") { calls += "handler" }
        }

        registry.dispatch(UiAction(type = "navigate", target = "home"))

        assertEquals(
            listOf("first-before", "second-before", "handler", "second-after", "first-after"),
            calls
        )
    }

    @Test
    fun deniedActionDoesNotReachInterceptors() {
        var intercepted = false
        val registry = ActionRegistry(
            interceptors = listOf(ActionInterceptor { _, _ -> intercepted = true }),
            actionPolicy = SduiActionPolicy { SduiActionDecision.Deny("Blocked") }
        )

        registry.dispatch(UiAction(type = "navigate"))

        assertFalse(intercepted)
    }
}
