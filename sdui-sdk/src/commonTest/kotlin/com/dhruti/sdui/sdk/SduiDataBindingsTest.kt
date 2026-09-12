package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SduiDataBindingsTest {
    @Test
    fun resolvesEmbeddedAndWholeValueBindingsWithoutLosingTypes() {
        val context = SduiDataContext(
            mapOf(
                "user" to SduiValue.ObjectValue(mapOf("name" to SduiValue.StringValue("Asha"))),
                "balance" to SduiValue.NumberValue(1250.0)
            )
        )

        assertEquals(
            "Hello, Asha",
            (SduiValue.StringValue("Hello, {{user.name}}").resolveBindings(context) as SduiValue.StringValue).value
        )
        assertIs<SduiValue.NumberValue>(SduiValue.StringValue("{{balance}}").resolveBindings(context))
    }

    @Test
    fun expandsRepeaterUsingItemAndIndexBindings() {
        val context = SduiDataContext(
            mapOf(
                "transactions" to SduiValue.ListValue(
                    listOf(
                        SduiValue.ObjectValue(mapOf("title" to SduiValue.StringValue("Coffee"))),
                        SduiValue.ObjectValue(mapOf("title" to SduiValue.StringValue("Book")))
                    )
                )
            )
        )
        val node = UiNode(
            type = "repeater",
            props = mapOf("items" to SduiValue.StringValue("{{transactions}}")),
            children = listOf(UiNode(type = "text", props = mapOf("value" to SduiValue.StringValue("{{index}}. {{item.title}}"))))
        )

        val resolved = node.resolveBindings(context)

        assertEquals("column", resolved.type)
        assertEquals(2, resolved.children.size)
        assertEquals("0.0. Coffee", (resolved.children[0].props["value"] as SduiValue.StringValue).value)
        assertEquals("1.0. Book", (resolved.children[1].props["value"] as SduiValue.StringValue).value)
    }
}
