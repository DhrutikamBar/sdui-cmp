package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiNode

/**
 * Flattens a nested UI tree into a list of nodes suitable for a single LazyColumn.
 */
object UiFlattener {
    
    fun flatten(node: UiNode): List<UiNode> {
        val result = mutableListOf<UiNode>()
        traverse(node, result)
        return result
    }

    private fun traverse(node: UiNode, result: MutableList<UiNode>) {
        val style = node.style()
        val isPureLayout = node.type in listOf("column", "row", "box") && 
                style.background == null && 
                style.cornerRadius == null && 
                style.padding == null &&
                style.arrangement == null &&
                style.alignment == null &&
                node.action == null &&
                node.visibleWhen.isEmpty()

        if (isPureLayout) {
            node.children.forEach { traverse(it, result) }
        } else {
            result.add(node)
        }
    }
}
