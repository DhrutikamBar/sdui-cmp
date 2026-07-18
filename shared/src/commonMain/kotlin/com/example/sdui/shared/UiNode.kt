package com.example.sdui.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A type-safe value wrapper for SDUI properties and state.
 */
@Serializable
sealed class SduiValue {
    @Serializable @SerialName("string") data class StringValue(val value: String) : SduiValue()
    @Serializable @SerialName("number") data class NumberValue(val value: Double) : SduiValue()
    @Serializable @SerialName("boolean") data class BooleanValue(val value: Boolean) : SduiValue()
    @Serializable @SerialName("list") data class ListValue(val value: List<SduiValue>) : SduiValue()
    @Serializable @SerialName("object") data class ObjectValue(val value: Map<String, SduiValue>) : SduiValue()
}

/**
 * A structured condition for rules and visibility.
 */
@Serializable
sealed class Condition {
    @Serializable @SerialName("equals") data class Equals(val field: String, val value: SduiValue) : Condition()
    @Serializable @SerialName("notEmpty") data class NotEmpty(val field: String) : Condition()
    @Serializable @SerialName("isTrue") data class IsTrue(val field: String) : Condition()
    @Serializable @SerialName("matches") data class Matches(val field: String, val regex: String) : Condition()
    @Serializable @SerialName("not") data class Not(val condition: Condition) : Condition()
    @Serializable @SerialName("and") data class And(val conditions: List<Condition>) : Condition()
    @Serializable @SerialName("or") data class Or(val conditions: List<Condition>) : Condition()
}

/**
 * Feedback types for user actions.
 */
@Serializable
sealed class Feedback {
    @Serializable @SerialName("haptic") data class Haptic(val type: String) : Feedback()
    @Serializable @SerialName("sound") data class Sound(val name: String) : Feedback()
}

/**
 * A single node in the server-driven UI tree.
 */
@Serializable
data class UiNode(
    val id: String? = null,
    val type: String,
    val props: Map<String, SduiValue> = emptyMap(),
    val children: List<UiNode> = emptyList(),
    val action: UiAction? = null,
    val rules: List<Condition> = emptyList(),
    val visibleWhen: List<Condition> = emptyList(),
    val errorWhen: List<Condition> = emptyList(),
    val fallback: UiNode? = null,
    val sticky: Boolean = false
)

/**
 * A server-defined action.
 */
@Serializable
data class UiAction(
    val type: String,
    val target: String? = null,
    val method: String? = null,
    val body: Map<String, SduiValue>? = null,
    val onSuccess: UiAction? = null,
    val onError: UiAction? = null,
    val feedback: Feedback? = null,
    val metadata: Map<String, SduiValue> = emptyMap()
)
