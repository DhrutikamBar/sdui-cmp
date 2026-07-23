package com.dhruti.sdui.sdk

import com.example.sdui.shared.Condition
import com.example.sdui.shared.SduiValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SduiLogicTest {

    @Test
    fun testConditionEvaluate() {
        val state = FormState(mapOf(
            "age" to SduiValue.NumberValue(20.0),
            "name" to SduiValue.StringValue("John"),
            "premium" to SduiValue.BooleanValue(true)
        ))

        assertTrue(Condition.Equals("name", SduiValue.StringValue("John")).evaluate(state))
        assertFalse(Condition.Equals("name", SduiValue.StringValue("Doe")).evaluate(state))

        assertTrue(Condition.NotEmpty("name").evaluate(state))
        assertFalse(Condition.NotEmpty("unknown").evaluate(state))

        assertTrue(Condition.IsTrue("premium").evaluate(state))
        assertFalse(Condition.IsTrue("unknown").evaluate(state))

        assertTrue(Condition.Matches("name", "^J.*").evaluate(state))
        assertFalse(Condition.Matches("name", "^D.*").evaluate(state))

        // Invalid regex should return false and not crash
        assertFalse(Condition.Matches("name", "[").evaluate(state))
    }

    @Test
    fun testScriptEvaluate() {
        val state = FormState(mapOf(
            "price" to SduiValue.NumberValue(50.0),
            "qty" to SduiValue.NumberValue(3.0)
        ))

        assertTrue(Condition.Script("price * qty > 100").evaluate(state))
        assertFalse(Condition.Script("price * qty < 100").evaluate(state))
        assertTrue(Condition.Script("price * qty == 150").evaluate(state))
    }
}
