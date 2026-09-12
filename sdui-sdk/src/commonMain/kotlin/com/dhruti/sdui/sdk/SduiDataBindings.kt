package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode

/**
 * Typed values owned by the host application and made available to an SDUI document.
 *
 * A document may reference a value using `{{name}}` or a property using
 * `{{account.balance}}`. The document only reads these values; it cannot mutate
 * the host's data model.
 */
data class SduiDataContext(
    val values: Map<String, SduiValue> = emptyMap()
) {
    operator fun get(path: String): SduiValue? {
        val segments = path.split('.').filter(String::isNotBlank)
        if (segments.isEmpty()) return null
        var current = values[segments.first()] ?: return null
        segments.drop(1).forEach { segment ->
            current = (current as? SduiValue.ObjectValue)?.value?.get(segment) ?: return null
        }
        return current
    }

    fun withValues(extra: Map<String, SduiValue>): SduiDataContext =
        SduiDataContext(values + extra)
}

/** Resolves template bindings without coercing a whole-value binding to text. */
fun SduiValue.resolveBindings(context: SduiDataContext): SduiValue = when (this) {
    is SduiValue.StringValue -> {
        val exact = BINDING.matchEntire(value)
        if (exact != null) {
            context[exact.groupValues[1]] ?: this
        } else {
            SduiValue.StringValue(BINDING.replace(value) { match ->
                context[match.groupValues[1]].displayValue()
            })
        }
    }
    is SduiValue.ListValue -> SduiValue.ListValue(value.map { it.resolveBindings(context) })
    is SduiValue.ObjectValue -> SduiValue.ObjectValue(value.mapValues { (_, item) ->
        item.resolveBindings(context)
    })
    else -> this
}

private fun SduiValue?.displayValue(): String = when (this) {
    is SduiValue.StringValue -> value
    is SduiValue.NumberValue -> value.toString()
    is SduiValue.BooleanValue -> value.toString()
    is SduiValue.ListValue, is SduiValue.ObjectValue, null -> ""
}

private val BINDING = Regex("""\{\{\s*([A-Za-z][A-Za-z0-9_.-]{0,127})\s*\}\}""")

/**
 * Produces a renderer-ready node tree. `repeater` is a lightweight list
 * primitive: put an `items: "{{transactions}}"` prop on the repeater and one
 * child template beneath it. Within the template use `{{item}}`,
 * `{{item.title}}`, and `{{index}}`.
 */
fun UiNode.resolveBindings(context: SduiDataContext): UiNode {
    val resolvedProps = props.mapValues { (_, value) -> value.resolveBindings(context) }
    val resolvedAction = action?.resolveBindings(context)
    val resolvedAppear = onAppear?.resolveBindings(context)
    val resolvedDisappear = onDisappear?.resolveBindings(context)

    if (type == "repeater") {
        val items = (resolvedProps["items"] as? SduiValue.ListValue)?.value.orEmpty()
        val template = children.firstOrNull()
        val renderedChildren = if (template == null) emptyList() else items.mapIndexed { index, item ->
            val itemFields = (item as? SduiValue.ObjectValue)?.value.orEmpty()
            template.resolveBindings(
                context.withValues(itemFields + mapOf(
                    "item" to item,
                    "index" to SduiValue.NumberValue(index.toDouble())
                ))
            )
        }
        return copy(
            type = "column",
            props = resolvedProps - "items",
            children = renderedChildren,
            action = resolvedAction,
            onAppear = resolvedAppear,
            onDisappear = resolvedDisappear,
            fallback = fallback?.resolveBindings(context)
        )
    }

    return copy(
        props = resolvedProps,
        children = children.map { it.resolveBindings(context) },
        action = resolvedAction,
        onAppear = resolvedAppear,
        onDisappear = resolvedDisappear,
        fallback = fallback?.resolveBindings(context)
    )
}

private fun UiAction.resolveBindings(context: SduiDataContext): UiAction = copy(
    target = target?.let { (SduiValue.StringValue(it).resolveBindings(context) as? SduiValue.StringValue)?.value ?: it },
    method = method?.let { (SduiValue.StringValue(it).resolveBindings(context) as? SduiValue.StringValue)?.value ?: it },
    body = body?.mapValues { (_, value) -> value.resolveBindings(context) },
    metadata = metadata.mapValues { (_, value) -> value.resolveBindings(context) },
    onSuccess = onSuccess?.resolveBindings(context),
    onError = onError?.resolveBindings(context)
)
