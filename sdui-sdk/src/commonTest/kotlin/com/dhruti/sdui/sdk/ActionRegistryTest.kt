package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import kotlin.test.Test
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
}
