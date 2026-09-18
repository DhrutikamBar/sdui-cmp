package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BottomNavigationContractTest {
    @Test
    fun bottomNavigationIsRegistered() {
        val registry = ComponentRegistry()
        registry.registerCoreWidgets()

        assertTrue(registry.supports("bottomNavigation"))
    }

    @Test
    fun rootBottomNavigationIsPreservedForPinnedRendering() {
        val root = UiNode(
            type = "column",
            children = listOf(
                UiNode(type = "text"),
                UiNode(
                    id = "mainNavigation",
                    type = "bottomNavigation",
                    props = mapOf(
                        "items" to SduiValue.ListValue(
                            listOf(
                                SduiValue.ObjectValue(
                                    mapOf("label" to SduiValue.StringValue("Home"))
                                )
                            )
                        )
                    )
                )
            )
        )

        assertEquals(
            listOf("text", "bottomNavigation"),
            rootNodesForRendering(root).map(UiNode::type)
        )
    }
}
