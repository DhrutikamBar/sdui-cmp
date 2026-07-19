package com.example.sdui.app

import com.example.sdui.shared.UiNode

/**
 * Flattens a nested UI tree into a list of nodes suitable for a single LazyColumn.
 * This avoids nested scrolling issues and improves overall performance.
 */
object UiFlattener {
    
    fun flatten(node: UiNode): List<UiNode> {
        val result = mutableListOf<UiNode>()
        traverse(node, result)
        return result
    }

    private fun traverse(node: UiNode, result: MutableList<UiNode>) {
        // Layout nodes (column/row/box) with specific styles might need to remain containers,
        // but simple layout wrappers should be flattened.
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
            // This node remains as an item in the list.
            // If it's a container that isn't "pure" (has background, padding, etc),
            // we keep it as one unit. The list/grid widgets are also units.
            result.add(node)
        }
    }
}
