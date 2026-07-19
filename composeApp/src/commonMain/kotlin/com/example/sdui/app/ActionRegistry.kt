package com.example.sdui.app

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiAction

/**
 * Intercepts actions before they are handled. Useful for analytics, confirmations, etc.
 */
interface ActionInterceptor {
    fun intercept(action: UiAction, next: (UiAction) -> Unit)
}

/**
 * Registry for action handlers and interceptors.
 */
class ActionRegistry(
    private val interceptors: List<ActionInterceptor> = emptyList()
) {
    private val handlers = mutableMapOf<String, (UiAction) -> Unit>()

    fun register(type: String, handler: (UiAction) -> Unit) {
        handlers[type] = handler
    }

    fun dispatch(action: UiAction) {
        var currentIndex = 0
        
        fun next(currentAction: UiAction) {
            if (currentIndex < interceptors.size) {
                val interceptor = interceptors[currentIndex]
                currentIndex++
                interceptor.intercept(currentAction, ::next)
            } else {
                val handler = handlers[currentAction.type]
                if (handler != null) {
                    handler(currentAction)
                } else {
                    println("Unhandled action type: ${currentAction.type}")
                }
            }
        }
        
        next(action)
    }
}

/** "{{fieldId}}" values in an apiCall body get swapped for that field's current value. */
fun interpolate(body: Map<String, SduiValue>?, formState: FormState): Map<String, SduiValue>? {
    body ?: return null
    return body.mapValues { (_, value) ->
        if (value is SduiValue.StringValue) {
            val content = value.value
            if (content.startsWith("{{") && content.endsWith("}}")) {
                val fieldId = content.removePrefix("{{").removeSuffix("}}")
                formState[fieldId] ?: value
            } else {
                value
            }
        } else {
            value
        }
    }
}
