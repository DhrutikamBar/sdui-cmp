package com.example.sdui.app

import com.dhruti.sdui.sdk.SduiActionDecision
import com.example.sdui.shared.UiAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DemoSduiActionPolicyTest {
    @Test
    fun allowsSupportedLocalActions() {
        assertEquals(
            SduiActionDecision.Allow,
            DemoSduiActionPolicy.evaluate(UiAction(type = "navigate", target = "checkout"))
        )
        assertEquals(
            SduiActionDecision.Allow,
            DemoSduiActionPolicy.evaluate(UiAction(type = "apiCall", method = "POST", target = "/api/orders"))
        )
        assertEquals(
            SduiActionDecision.Allow,
            DemoSduiActionPolicy.evaluate(UiAction(type = "openUrl", target = "https://example.com"))
        )
        assertEquals(
            SduiActionDecision.Allow,
            DemoSduiActionPolicy.evaluate(UiAction(type = "analytics", target = "sign_up_header_impression"))
        )
        assertEquals(
            SduiActionDecision.Allow,
            DemoSduiActionPolicy.evaluate(UiAction(type = "refreshData"))
        )
    }

    @Test
    fun deniesUnknownAndUnsafeActions() {
        assertIs<SduiActionDecision.Deny>(
            DemoSduiActionPolicy.evaluate(UiAction(type = "deleteAccount"))
        )
        assertIs<SduiActionDecision.Deny>(
            DemoSduiActionPolicy.evaluate(UiAction(type = "openUrl", target = "http://example.com"))
        )
        assertIs<SduiActionDecision.Deny>(
            DemoSduiActionPolicy.evaluate(UiAction(type = "apiCall", method = "TRACE", target = "/api/orders"))
        )
        assertIs<SduiActionDecision.Deny>(
            DemoSduiActionPolicy.evaluate(UiAction(type = "apiCall", target = "//untrusted.example"))
        )
    }
}
