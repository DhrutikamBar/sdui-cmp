package com.example.sdui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdui.shared.Condition
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Anything a widget's onClick/navigation should trigger. Each host app supplies its own. */
fun interface ActionHandler {
    fun handle(action: UiAction)
}

/** Shared, observable state used for rule evaluation. */
class FormState(initialValues: Map<String, SduiValue> = emptyMap()) {
    val values = mutableStateMapOf<String, SduiValue>().apply { putAll(initialValues) }
    
    operator fun set(key: String, value: SduiValue) {
        values[key] = value
    }
    operator fun get(key: String): SduiValue? = values[key]

    /** Convenience for text inputs. */
    fun getString(key: String): String = (values[key] as? SduiValue.StringValue)?.value ?: ""
    fun setString(key: String, value: String) {
        values[key] = SduiValue.StringValue(value)
    }

    companion object {
        val Saver: Saver<FormState, Map<String, String>> = Saver(
            save = { state -> 
                state.values.mapValues { Json.encodeToString(it.value) } 
            },
            restore = { savedMap -> 
                val restored = savedMap.mapValues { Json.decodeFromString<SduiValue>(it.value) }
                FormState(restored)
            }
        )
    }
}

/** Evaluates complex conditions against the current form state. */
fun Condition.evaluate(state: FormState): Boolean {
    return when (this) {
        is Condition.Equals -> state[field] == value
        is Condition.NotEmpty -> {
            val v = state[field]
            when (v) {
                is SduiValue.StringValue -> v.value.isNotEmpty()
                is SduiValue.ListValue -> v.value.isNotEmpty()
                is SduiValue.ObjectValue -> v.value.isNotEmpty()
                null -> false
                else -> true
            }
        }
        is Condition.IsTrue -> state[field] is SduiValue.BooleanValue && (state[field] as SduiValue.BooleanValue).value
        is Condition.Matches -> {
            val v = state[field]
            if (v is SduiValue.StringValue) Regex(regex).matches(v.value) else false
        }
        is Condition.Not -> !condition.evaluate(state)
        is Condition.And -> conditions.all { it.evaluate(state) }
        is Condition.Or -> conditions.any { it.evaluate(state) }
        is Condition.Script -> evaluateScript(expression, state)
    }
}

/** 
 * A robust expression evaluator for SDUI.
 * Supports multiple variables from FormState and basic arithmetic.
 * Example: "price * qty > 100"
 */
private fun evaluateScript(expression: String, state: FormState): Boolean {
    // 1. Replace variables with their values
    var interpolated = expression
    state.values.forEach { (key, value) ->
        val num = when (value) {
            is SduiValue.NumberValue -> value.value
            is SduiValue.StringValue -> value.value.toDoubleOrNull()
            else -> null
        }
        if (num != null) {
            interpolated = interpolated.replace(key, num.toString())
        }
    }
    
    // 2. Basic math parser for comparisons
    val ops = listOf(">=", "<=", "==", ">", "<")
    val op = ops.find { interpolated.contains(it) } ?: return false
    val parts = interpolated.split(op, limit = 2)
    if (parts.size != 2) return false
    
    val left = evaluateMath(parts[0])
    val right = evaluateMath(parts[1])
    
    return when (op) {
        ">" -> left > right
        "<" -> left < right
        "==" -> left == right
        ">=" -> left >= right
        "<=" -> left <= right
        else -> false
    }
}

private fun evaluateMath(expr: String): Double {
    val clean = expr.trim()
    // Support basic multiplication for "total == price * qty"
    if (clean.contains("*")) {
        val parts = clean.split("*")
        return parts.map { it.trim().toDoubleOrNull() ?: 0.0 }.reduce { acc, d -> acc * d }
    }
    return clean.toDoubleOrNull() ?: 0.0
}

/** Tells children whether they are inside a scrollable container. */
val LocalIsInsideScrollable = compositionLocalOf { false }

/**
 * Maps a node's `type` string to the Composable that renders it.
 */
class ComponentRegistry {
    private val renderers =
        mutableMapOf<String, @Composable (UiNode, ActionHandler, FormState) -> Unit>()

    fun register(type: String, renderer: @Composable (UiNode, ActionHandler, FormState) -> Unit) {
        renderers[type] = renderer
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RenderRoot(node: UiNode, actions: ActionHandler, formState: FormState) {
        // Promote the root style to the LazyColumn itself
        val rootStyle = node.style()
        
        // We render the root container's children as top-level items in the LazyColumn.
        // If the root isn't a layout, we just render the root itself as the single item.
        val flattenedNodes = if (node.type in listOf("column", "row", "box")) {
            node.children.flatMap { UiFlattener.flatten(it) }
        } else {
            UiFlattener.flatten(node)
        }
        
        CompositionLocalProvider(LocalIsInsideScrollable provides true) {
            LazyColumn(Modifier.fillMaxSize().applyStyle(rootStyle)) {
                flattenedNodes.forEach { itemNode ->
                    if (itemNode.sticky) {
                        stickyHeader(key = itemNode.id ?: itemNode.hashCode()) {
                            Render(itemNode, actions, formState)
                        }
                    } else {
                        item(key = itemNode.id ?: itemNode.hashCode()) {
                            Render(itemNode, actions, formState)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Render(node: UiNode, actions: ActionHandler, formState: FormState) {
        val renderer = renderers[node.type]
        val reporter = LocalReportingService.current

        if (renderer == null) {
            val context = mapOf("type" to node.type, "id" to (node.id ?: "unnamed"))
            reporter.reportEvent("missing_renderer", context)
            
            // Graceful degradation: try fallback if it exists
            node.fallback?.let { Render(it, actions, formState) }
            return
        }

        val content: @Composable () -> Unit = {
            DisposableEffect(node.id) {
                node.onAppear?.let { actions.handle(it) }
                onDispose {
                    node.onDisappear?.let { actions.handle(it) }
                }
            }
            renderer(node, actions, formState)
        }

        if (node.visibleWhen.isEmpty()) {
            content()
            return
        }

        val style = node.style()
        val visible = node.visibleWhen.all { it.evaluate(formState) }
        AnimatedVisibility(
            visible = visible,
            enter = enterAnimation(style.animation),
            exit = exitAnimation(style.animation)
        ) {
            content()
        }
    }
}
