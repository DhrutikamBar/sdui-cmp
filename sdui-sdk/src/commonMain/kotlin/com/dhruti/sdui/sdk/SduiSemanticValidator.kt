package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode

class SduiSemanticValidationException(message: String) : IllegalArgumentException(message)

object SduiSemanticValidator {
    fun validate(
        root: UiNode,
        isWidgetSupported: (String) -> Boolean,
        isActionSupported: (String) -> Boolean = { true }
    ) {
        val nodes = mutableListOf(root)
        while (nodes.isNotEmpty()) {
            val node = nodes.removeAt(nodes.lastIndex)
            if (!isWidgetSupported(node.type) && node.fallback == null) {
                throw SduiSemanticValidationException(
                    "Unsupported widget type without fallback: ${node.type}"
                )
            }
            node.action?.let { validateAction(it, isActionSupported) }
            node.onAppear?.let { validateAction(it, isActionSupported) }
            node.onDisappear?.let { validateAction(it, isActionSupported) }
            node.fallback?.let(nodes::add)
            nodes.addAll(node.children)
        }
    }

    private fun validateAction(action: UiAction, isActionSupported: (String) -> Boolean) {
        val actions = mutableListOf(action)
        while (actions.isNotEmpty()) {
            val current = actions.removeAt(actions.lastIndex)
            if (!isActionSupported(current.type)) {
                throw SduiSemanticValidationException(
                    "Unsupported action type: ${current.type}"
                )
            }
            current.onSuccess?.let(actions::add)
            current.onError?.let(actions::add)
        }
    }
}
