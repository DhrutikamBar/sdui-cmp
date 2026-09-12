package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiAction

/**
 * Intercepts actions before they are handled. Useful for analytics, confirmations, etc.
 */
interface ActionInterceptor {
    fun intercept(action: UiAction, next: (UiAction) -> Unit)
}

/** Outcome of synchronous action dispatch. Asynchronous hosts can use this to
 * report that a request was accepted, then report their own completion outcome. */
sealed interface SduiActionDispatchResult {
    data object Dispatched : SduiActionDispatchResult
    data class Denied(val reason: String) : SduiActionDispatchResult
    data object Unhandled : SduiActionDispatchResult
}

/**
 * Registry for action handlers and interceptors.
 */
class ActionRegistry(
    private val interceptors: List<ActionInterceptor> = emptyList(),
    private val actionPolicy: SduiActionPolicy = AllowAllSduiActionPolicy
) {
    private val handlers = mutableMapOf<String, (UiAction) -> Unit>()

    fun register(type: String, handler: (UiAction) -> Unit) {
        handlers[type] = handler
    }

    fun dispatch(action: UiAction): SduiActionDispatchResult {
        when (val decision = actionPolicy.evaluate(action)) {
            SduiActionDecision.Allow -> Unit
            is SduiActionDecision.Deny -> {
                println("Denied action type: ${action.type}. Reason: ${decision.reason}")
                return SduiActionDispatchResult.Denied(decision.reason)
            }
        }

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

        val hasHandler = handlers.containsKey(action.type)
        next(action)
        return if (hasHandler) SduiActionDispatchResult.Dispatched else SduiActionDispatchResult.Unhandled
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
