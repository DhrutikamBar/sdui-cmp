package com.example.sdui.shared

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * A type-safe value wrapper for SDUI properties and state.
 * Uses a custom serializer to support "natural" JSON (no type discriminators).
 */
@Serializable(with = SduiValueSerializer::class)
sealed class SduiValue {
    @Serializable(with = StringValueSerializer::class)
    @SerialName("string") data class StringValue(val value: String) : SduiValue()

    @Serializable(with = NumberValueSerializer::class)
    @SerialName("number") data class NumberValue(val value: Double) : SduiValue()

    @Serializable(with = BooleanValueSerializer::class)
    @SerialName("boolean") data class BooleanValue(val value: Boolean) : SduiValue()

    @Serializable(with = ListValueSerializer::class)
    @SerialName("list") data class ListValue(val value: List<SduiValue>) : SduiValue()

    @Serializable(with = ObjectValueSerializer::class)
    @SerialName("object") data class ObjectValue(val value: Map<String, SduiValue>) : SduiValue()
}

/**
 * Polymorphic serializer for SduiValue that detects type based on JSON content.
 */
object SduiValueSerializer : JsonContentPolymorphicSerializer<SduiValue>(SduiValue::class) {
    override fun selectDeserializer(element: JsonElement) = when (element) {
        is JsonPrimitive -> {
            when {
                element.isString -> SduiValue.StringValue.serializer()
                element.booleanOrNull != null -> SduiValue.BooleanValue.serializer()
                else -> SduiValue.NumberValue.serializer()
            }
        }
        is JsonArray -> SduiValue.ListValue.serializer()
        is JsonObject -> SduiValue.ObjectValue.serializer()
    }
}

object StringValueSerializer : KSerializer<SduiValue.StringValue> {
    override val descriptor = PrimitiveSerialDescriptor("StringValue", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SduiValue.StringValue) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder) = SduiValue.StringValue(decoder.decodeString())
}

object NumberValueSerializer : KSerializer<SduiValue.NumberValue> {
    override val descriptor = PrimitiveSerialDescriptor("NumberValue", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: SduiValue.NumberValue) = encoder.encodeDouble(value.value)
    override fun deserialize(decoder: Decoder) = SduiValue.NumberValue(decoder.decodeDouble())
}

object BooleanValueSerializer : KSerializer<SduiValue.BooleanValue> {
    override val descriptor = PrimitiveSerialDescriptor("BooleanValue", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: SduiValue.BooleanValue) = encoder.encodeBoolean(value.value)
    override fun deserialize(decoder: Decoder) = SduiValue.BooleanValue(decoder.decodeBoolean())
}

object ListValueSerializer : KSerializer<SduiValue.ListValue> {
    override val descriptor = ListSerializer(SduiValue.serializer()).descriptor
    override fun serialize(encoder: Encoder, value: SduiValue.ListValue) = 
        encoder.encodeSerializableValue(ListSerializer(SduiValue.serializer()), value.value)
    override fun deserialize(decoder: Decoder) = 
        SduiValue.ListValue(decoder.decodeSerializableValue(ListSerializer(SduiValue.serializer())))
}

object ObjectValueSerializer : KSerializer<SduiValue.ObjectValue> {
    override val descriptor = MapSerializer(String.serializer(), SduiValue.serializer()).descriptor
    override fun serialize(encoder: Encoder, value: SduiValue.ObjectValue) = 
        encoder.encodeSerializableValue(MapSerializer(String.serializer(), SduiValue.serializer()), value.value)
    override fun deserialize(decoder: Decoder) = 
        SduiValue.ObjectValue(decoder.decodeSerializableValue(MapSerializer(String.serializer(), SduiValue.serializer())))
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
    @Serializable @SerialName("script") data class Script(val expression: String) : Condition()
}

/**
 * Feedback types for user actions.
 */
@Serializable
sealed class Feedback {
    @Serializable @SerialName("haptic") data class Haptic(val intensity: String) : Feedback()
    @Serializable @SerialName("sound") data class Sound(val name: String) : Feedback()
}

/**
 * Accessibility metadata for screen readers.
 */
@Serializable
data class Semantics(
    val contentDescription: String? = null,
    val role: String? = null, // "button", "image", "header"
    val liveRegion: String? = null // "none", "polite", "assertive"
)

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
    val sticky: Boolean = false,
    val semantics: Semantics? = null,
    val minSdkVersion: Int? = null,
    val onAppear: UiAction? = null,
    val onDisappear: UiAction? = null
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
