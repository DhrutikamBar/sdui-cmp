package com.dhruti.sdui.sdk

import com.example.sdui.shared.Condition
import com.example.sdui.shared.SduiValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SduiExpressionEvaluatorTest {
    private val state = FormState(
        mapOf(
            "amount" to SduiValue.NumberValue(25.0),
            "quantity" to SduiValue.NumberValue(4.0),
            "status" to SduiValue.StringValue("active"),
            "enabled" to SduiValue.BooleanValue(true)
        )
    )

    @Test
    fun evaluatesSupportedComparisonsAndProducts() {
        assertTrue(Condition.Script("amount * quantity >= 100").evaluate(state))
        assertTrue(Condition.Script("status == 'active'").evaluate(state))
        assertTrue(Condition.Script("enabled == true").evaluate(state))
    }

    @Test
    fun rejectsUnknownOrExecutableSyntax() {
        assertFalse(Condition.Script("unknown == 1").evaluate(state))
        assertFalse(Condition.Script("amount() == 25").evaluate(state))
        assertFalse(Condition.Script("amount = 25").evaluate(state))
    }
}
