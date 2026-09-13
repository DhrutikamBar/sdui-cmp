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

    /** Returns a display value without discarding an input's underlying type. */
    fun getString(key: String): String = when (val value = values[key]) {
        is SduiValue.StringValue -> value.value
        is SduiValue.NumberValue -> value.value.toString()
        is SduiValue.BooleanValue -> value.value.toString()
        else -> ""
    }

    fun setString(key: String, value: String) {
        values[key] = SduiValue.StringValue(value)
    }

    /**
     * Stores number keyboard input as a NumberValue whenever it is valid.
     * Invalid and empty input remains visible as text so the field can be corrected.
     */
    fun setTextInput(key: String, value: String, keyboardType: String) {
        values[key] = if (keyboardType == "number") {
            value.toDoubleOrNull()?.let(SduiValue::NumberValue) ?: SduiValue.StringValue(value)
        } else {
            SduiValue.StringValue(value)
        }
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
        is Condition.Script -> SduiExpressionEvaluator.evaluate(expression, state)
    }
}

/** The node set consumed by RenderRoot's single scroll owner. */
internal fun rootNodesForRendering(node: UiNode): List<UiNode> = UiFlattener.flattenRoot(node)

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

    fun supports(type: String): Boolean = type in renderers

    /** Snapshot of renderer types available to the current host. */
    fun supportedTypes(): Set<String> = renderers.keys.toSet()

    /** Capability payload for backend document selection or host validation. */
    fun capabilities(actionTypes: Set<String>): SduiCapabilities =
        SduiCapabilities(widgetTypes = supportedTypes(), actionTypes = actionTypes)

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RenderRoot(
        node: UiNode,
        actions: ActionHandler,
        formState: FormState,
        modifier: Modifier = Modifier
    ) {
        // Enforce root version check
        val minSdk = node.minSdkVersion
        if (minSdk != null && minSdk > SDK_VERSION) {
            Text("App update required to view this content")
            return
        }

        // UiFlattener preserves root rows, boxes, and styled columns as nodes.
        // LazyColumn remains the only scroll owner at this level.
        val rootNodes = rootNodesForRendering(node)

        CompositionLocalProvider(LocalIsInsideScrollable provides true) {
            LazyColumn(modifier.fillMaxSize()) {
                rootNodes.forEachIndexed { index, itemNode ->
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
        if (minSdk != null && minSdk > SDK_VERSION) {
            node.fallback?.let { Render(it, actions, formState) }
            return
        }

        val renderer = renderers[node.type]
        val reporter = LocalReportingService.current

        if (renderer == null) {
            val context = mapOf("type" to node.type, "id" to (node.id ?: "unnamed"))
            reporter.reportEvent("missing_renderer", context)
            
            node.fallback?.let { Render(it, actions, formState) }
                ?: Text("Unsupported component")
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
