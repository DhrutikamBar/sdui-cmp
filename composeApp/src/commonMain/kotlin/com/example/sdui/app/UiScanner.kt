package com.example.sdui.app

import com.example.sdui.shared.UiNode

object UiScanner {
    
    /** Scans for all navigation paths in the tree. */
    fun findNavigablePaths(node: UiNode): Set<String> {
        val paths = mutableSetOf<String>()
        traverse(node) { 
            if (it.action?.type == "navigate") {
                it.action?.target?.let { target -> paths.add(target) }
            }
        }
        return paths
    }

    private fun traverse(node: UiNode, action: (UiNode) -> Unit) {
        action(node)
        node.children.forEach { traverse(it, action) }
        node.fallback?.let { traverse(it, action) }
    }
}
