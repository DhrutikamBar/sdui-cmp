package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SduiSemanticValidatorTest {
    @Test
    fun acceptsUnsupportedWidgetWhenItHasASupportedFallback() {
        SduiSemanticValidator.validate(
            root = UiNode(
                type = "newWidget",
                fallback = UiNode(type = "text")
            ),
            isWidgetSupported = { it == "text" }
        )
    }

    @Test
    fun rejectsUnsupportedWidgetWithoutFallback() {
        assertFailsWith<SduiSemanticValidationException> {
            SduiSemanticValidator.validate(
                root = UiNode(type = "newWidget"),
                isWidgetSupported = { it == "text" }
            )
        }
    }

    @Test
    fun validatesLifecycleAndChainedActions() {
        val root = UiNode(
            type = "text",
            onAppear = UiAction(
                type = "analytics",
                onSuccess = UiAction(type = "navigate")
            ),
            onDisappear = UiAction(type = "analytics")
        )

        assertFailsWith<SduiSemanticValidationException> {
            SduiSemanticValidator.validate(
                root = root,
                isWidgetSupported = { it == "text" },
                isActionSupported = { it == "analytics" }
            )
        }
    }
}
