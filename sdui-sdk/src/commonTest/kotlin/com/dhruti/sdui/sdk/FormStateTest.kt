package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FormStateTest {
    @Test
    fun storesValidNumberInputAsANumberValue() {
        val state = FormState()

        state.setTextInput("amount", "12.50", "number")

        assertEquals(SduiValue.NumberValue(12.5), state["amount"])
        assertEquals("12.5", state.getString("amount"))
    }

    @Test
    fun preservesInvalidNumberInputAsVisibleText() {
        val state = FormState()

        state.setTextInput("amount", "12x", "number")

        assertIs<SduiValue.StringValue>(state["amount"])
        assertEquals("12x", state.getString("amount"))
    }

    @Test
    fun storesTextInputAsAStringValue() {
        val state = FormState()

        state.setTextInput("name", "Dhruti", "text")

        assertEquals(SduiValue.StringValue("Dhruti"), state["name"])
    }
}
