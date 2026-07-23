package com.dhruti.sdui.sdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
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
            if (v is SduiValue.StringValue) {
                try {
                    Regex(regex).matches(v.value)
                } catch (e: Exception) {
                    false
                }
            } else false
        }
        is Condition.Not -> !condition.evaluate(state)
        is Condition.And -> conditions.all { it.evaluate(state) }
        is Condition.Or -> conditions.any { it.evaluate(state) }
        is Condition.Script -> evaluateScript(expression, state)
    }
}

/** 
 * A robust expression evaluator for SDUI.
 * Supports multiple variables from FormState, literals, and basic arithmetic.
 */
private fun evaluateScript(expression: String, state: FormState): Boolean {
    val ops = listOf(">=", "<=", "==", ">", "<")
    val op = ops.find { expression.contains(it) } ?: return false
    val parts = expression.split(op, limit = 2)
    if (parts.size != 2) return false

    val left = evaluateExpressionPart(parts[0], state)
    val right = evaluateExpressionPart(parts[1], state)

    if (left == null || right == null) return false

    return when (op) {
        "==" -> left == right
        ">" -> if (left is Double && right is Double) left > right else false
        "<" -> if (left is Double && right is Double) left < right else false
        ">=" -> if (left is Double && right is Double) left >= right else false
        "<=" -> if (left is Double && right is Double) left <= right else false
        else -> false
    }
}

private fun evaluateExpressionPart(part: String, state: FormState): Any? {
    val raw = part.trim()
    
    if (raw.contains("*")) {
        val subParts = raw.split("*")
        return subParts.map { evaluateExpressionPart(it, state) as? Double ?: 0.0 }
            .reduce { acc, d -> acc * d }
    }

    // 1. Resolve from FormState
    state[raw]?.let { sduiVal ->
        return when (sduiVal) {
            is SduiValue.StringValue -> sduiVal.value
            is SduiValue.NumberValue -> sduiVal.value
            is SduiValue.BooleanValue -> sduiVal.value
            else -> null
        }
    }
    
    // 2. Literals
    if (raw.startsWith("'") && raw.endsWith("'")) return raw.removeSurrounding("'")
    if (raw == "true") return true
    if (raw == "false") return false
    
    return raw.toDoubleOrNull()
}

/** Tells children whether they are inside a scrollable container. */
val LocalIsInsideScrollable = compositionLocalOf { false }

private const val CURRENT_SDK_VERSION = 5

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
        // Enforce root version check
        val minSdk = node.minSdkVersion
        if (minSdk != null && minSdk > CURRENT_SDK_VERSION) {
            Text("App update required to view this content")
            return
        }

        val rootStyle = node.style()

        val flattenedNodes = if (node.type in listOf("column", "row", "box")) {
            node.children.flatMap { UiFlattener.flatten(it) }
        } else {
            UiFlattener.flatten(node)
        }

        CompositionLocalProvider(LocalIsInsideScrollable provides true) {
            LazyColumn(Modifier.fillMaxSize().applyStyle(rootStyle)) {
                flattenedNodes.forEachIndexed { index, itemNode ->
                    val key = itemNode.id ?: "item_$index"
                    if (itemNode.sticky) {
                        stickyHeader(key = key) {
                            Render(itemNode, actions, formState)
                        }
                    } else {
                        item(key = key) {
                            Render(itemNode, actions, formState)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Render(node: UiNode, actions: ActionHandler, formState: FormState) {
        // Version enforcement
        val minSdk = node.minSdkVersion
        if (minSdk != null && minSdk > CURRENT_SDK_VERSION) {
            node.fallback?.let { Render(it, actions, formState) }
            return
        }

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
