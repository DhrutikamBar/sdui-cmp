package com.example.sdui.app

import com.example.sdui.shared.UiAction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Same idea as ComponentRegistry, applied to actions instead of widgets: register a handler
 * per action type instead of hardcoding an ever-growing when-block in App.kt. New action
 * types ("share", "call", "analytics"...) are one more register() call, not a bigger when.
 */
class ActionRegistry {
    private val handlers = mutableMapOf<String, (UiAction) -> Unit>()

    fun register(type: String, handler: (UiAction) -> Unit) {
        handlers[type] = handler
    }

    fun dispatch(action: UiAction) {
        val handler = handlers[action.type]
        if (handler != null) handler(action) else println("Unhandled action type: ${action.type}")
    }
}

/** "{{fieldId}}" string values in an apiCall body get swapped for that field's current value —
 *  this is what lets the backend define a request body without knowing field values in advance. */
fun interpolate(body: JsonObject?, formState: FormState): JsonObject? {
    body ?: return null
    return JsonObject(
        body.mapValues { (_, value) ->
            val content = value.jsonPrimitive.contentOrNull
            if (content != null && content.startsWith("{{") && content.endsWith("}}")) {
                val fieldId = content.removePrefix("{{").removeSuffix("}}")
                JsonPrimitive(formState[fieldId] ?: "")
            } else {
                value
            }
        }
    )
}