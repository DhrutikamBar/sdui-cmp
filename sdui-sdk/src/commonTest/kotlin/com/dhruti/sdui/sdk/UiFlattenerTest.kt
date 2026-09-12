package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import kotlin.test.Test
import kotlin.test.assertEquals

class UiFlattenerTest {
    @Test
    fun flattensTransparentColumnRoot() {
        val root = UiNode(
            type = "column",
            children = listOf(UiNode(type = "text"), UiNode(type = "button"))
        )

        assertEquals(listOf("text", "button"), rootNodesForRendering(root).map(UiNode::type))
    }

    @Test
    fun preservesRowRootAsASingleLayoutNode() {
        val root = UiNode(
            type = "row",
            children = listOf(UiNode(type = "text"), UiNode(type = "button"))
        )

        assertEquals(listOf("row"), rootNodesForRendering(root).map(UiNode::type))
    }

    @Test
    fun preservesBoxRootAsASingleLayoutNode() {
        val root = UiNode(type = "box", children = listOf(UiNode(type = "text")))

        assertEquals(listOf("box"), rootNodesForRendering(root).map(UiNode::type))
    }

    @Test
    fun preservesStyledColumnRoot() {
        val root = UiNode(
            type = "column",
            props = mapOf(
                "style" to SduiValue.ObjectValue(
                    mapOf("padding" to SduiValue.StringValue("md"))
                )
            ),
            children = listOf(UiNode(type = "text"))
        )

        assertEquals(listOf("column"), rootNodesForRendering(root).map(UiNode::type))
    }
}
