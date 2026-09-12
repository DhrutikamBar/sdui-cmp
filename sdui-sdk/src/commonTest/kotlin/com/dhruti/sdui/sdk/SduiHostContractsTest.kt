package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import kotlin.test.Test
import kotlin.test.assertEquals

class SduiHostContractsTest {
    @Test
    fun allowAllPolicyExplicitlyAllowsAnAction() {
        val action = UiAction(type = "navigate", target = "home")

        assertEquals(SduiActionDecision.Allow, AllowAllSduiActionPolicy.evaluate(action))
    }

    @Test
    fun denialCarriesTheHostReason() {
        val policy = SduiActionPolicy { SduiActionDecision.Deny("Action type is not allowed") }

        assertEquals(
            SduiActionDecision.Deny("Action type is not allowed"),
            policy.evaluate(UiAction(type = "apiCall"))
        )
    }
}
