package com.example.sdui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import com.example.sdui.shared.Rule
import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode

/** Anything a widget's onClick/navigation should trigger. Each host app supplies its own. */
fun interface ActionHandler {
    fun handle(action: UiAction)
}

/** Shared, observable state used for rule evaluation — e.g. "is ageField non-empty". */
class FormState {
    val values = mutableStateMapOf<String, String>()
    operator fun set(key: String, value: String) {
        values[key] = value
    }
    operator fun get(key: String): String? = values[key]
}

/** notEmpty is the only check this demo implements — add more branches as you need them. */
fun Rule.isSatisfied(state: FormState): Boolean {
    val parts = whenExpr.split(".", limit = 2)
    if (parts.size != 2) return false
    val (field, check) = parts
    return when (check) {
        "notEmpty" -> !state[field].isNullOrEmpty()
        else -> false
    }
}

/**
 * Maps a node's `type` string to the Composable that renders it.
 * A host app calls `register` for its own custom widgets at startup (see Widgets.kt for
 * the built-in set this demo ships with) — this class never needs to change to support them.
 */
class ComponentRegistry {
    private val renderers =
        mutableMapOf<String, @Composable (UiNode, ActionHandler, FormState) -> Unit>()

    fun register(type: String, renderer: @Composable (UiNode, ActionHandler, FormState) -> Unit) {
        renderers[type] = renderer
    }

    @Composable
    fun Render(node: UiNode, actions: ActionHandler, formState: FormState) {
        val renderer = renderers[node.type]
        if (renderer != null) {
            renderer(node, actions, formState)
        } else {
            // Never crash on a type this build doesn't recognize — render nothing instead.
            // A production version might log this or show a neutral placeholder box.
        }
    }
}
