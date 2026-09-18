package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiNode

/**
 * Flattens only transparent column containers for a single LazyColumn.
 *
 * Rows and boxes carry layout meaning and must remain renderable nodes. Styled,
 * scrollable, actionable, or lifecycle-aware columns also remain intact.
 */
object UiFlattener {
    fun flattenRoot(node: UiNode): List<UiNode> =
        if (isTransparentColumn(node)) {
            node.children.flatMap(::flatten)
        } else {
            listOf(node)
        }

    fun flatten(node: UiNode): List<UiNode> {
        val result = mutableListOf<UiNode>()
        traverse(node, result)
        return result
    }

    private fun traverse(node: UiNode, result: MutableList<UiNode>) {
        if (isTransparentColumn(node)) {
            node.children.forEach { traverse(it, result) }
        } else {
            result += node
        }
    }

    private fun isTransparentColumn(node: UiNode): Boolean {
        val style = node.style()
        return node.type == "column" &&
            node.id == null &&
            node.action == null &&
            node.onAppear == null &&
            node.onDisappear == null &&
            node.rules.isEmpty() &&
            node.visibleWhen.isEmpty() &&
            node.errorWhen.isEmpty() &&
            node.fallback == null &&
            node.semantics == null &&
            !node.sticky &&
            style.padding == null &&
            style.background == null &&
            style.cornerRadius == null &&
            style.shape == null &&
            style.color == null &&
            style.fontSize == null &&
            style.fontWeight == null &&
            style.arrangement == null &&
            style.alignment == null &&
            style.width == null &&
            style.size == null &&
            style.scrollable != true &&
            style.animation == null &&
            style.animateSize != true
    }
}
