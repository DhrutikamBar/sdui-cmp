package com.example.sdui.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/** The currently supported wire format for an SDUI document. */
const val CURRENT_SDUI_SCHEMA_VERSION: Int = 1

/** Versioned SDUI payload. */
@Serializable
data class SduiDocument(
    val schemaVersion: Int = CURRENT_SDUI_SCHEMA_VERSION,
    val root: UiNode
)

/**
 * Limits applied while accepting untrusted SDUI content.
 *
 * Hosts may supply stricter limits to [SduiDocumentCodec]. The defaults are
 * intended to accommodate ordinary mobile screens while bounding CPU and memory.
 */
data class SduiPayloadLimits(
    val maxPayloadChars: Int = 1_000_000,
    val maxNodeCount: Int = 1_000,
    val maxNodeDepth: Int = 64,
    val maxActionDepth: Int = 16,
    val maxConditionDepth: Int = 16,
    val maxValueDepth: Int = 16,
    val maxStringLength: Int = 16_384,
    val maxObjectEntries: Int = 200,
    val maxListItems: Int = 200
) {
    init {
        require(maxPayloadChars > 0)
        require(maxNodeCount > 0)
        require(maxNodeDepth > 0)
        require(maxActionDepth > 0)
        require(maxConditionDepth > 0)
        require(maxValueDepth > 0)
        require(maxStringLength > 0)
        require(maxObjectEntries > 0)
        require(maxListItems > 0)
    }
}

/** Thrown when a payload cannot be safely accepted by the current SDK. */
class SduiPayloadValidationException(message: String) : IllegalArgumentException(message)

/**
 * Decodes and validates SDUI payloads at the untrusted-content boundary.
 *
 * Both the versioned [SduiDocument] envelope and legacy bare [UiNode] payloads
 * are accepted. Legacy payloads are normalized to schema version 1.
 */
object SduiDocumentCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun decode(
        payload: String,
        limits: SduiPayloadLimits = SduiPayloadLimits()
    ): SduiDocument {
        if (payload.length > limits.maxPayloadChars) {
            fail("Payload exceeds ${limits.maxPayloadChars} characters")
        }
        return decode(json.parseToJsonElement(payload), limits)
    }

    fun decode(
        element: JsonElement,
        limits: SduiPayloadLimits = SduiPayloadLimits()
    ): SduiDocument {
        if (element.toString().length > limits.maxPayloadChars) {
            fail("Payload exceeds ${limits.maxPayloadChars} characters")
        }
        val document = if (element is JsonObject && "root" in element) {
            json.decodeFromJsonElement<SduiDocument>(element)
        } else {
            SduiDocument(root = json.decodeFromJsonElement<UiNode>(element))
        }
        return validate(document, limits)
    }

    fun encode(
        document: SduiDocument,
        limits: SduiPayloadLimits = SduiPayloadLimits()
    ): String = json.encodeToString(validate(document, limits))

    fun fromLegacy(
        root: UiNode,
        limits: SduiPayloadLimits = SduiPayloadLimits()
    ): SduiDocument = validate(SduiDocument(root = root), limits)

    fun validate(
        document: SduiDocument,
        limits: SduiPayloadLimits = SduiPayloadLimits()
    ): SduiDocument {
        if (document.schemaVersion != CURRENT_SDUI_SCHEMA_VERSION) {
            fail("Unsupported SDUI schema version: ${document.schemaVersion}")
        }
        validateNodeTree(document.root, limits)
        return document
    }

    private fun validateNodeTree(root: UiNode, limits: SduiPayloadLimits) {
        val pending = mutableListOf(root to 1)
        var nodeCount = 0

        while (pending.isNotEmpty()) {
            val (node, depth) = pending.removeAt(pending.lastIndex)
            if (depth > limits.maxNodeDepth) fail("Node depth exceeds ${limits.maxNodeDepth}")
            nodeCount++
            if (nodeCount > limits.maxNodeCount) fail("Node count exceeds ${limits.maxNodeCount}")

            validateString(node.type, "node type", limits)
            node.id?.let { validateString(it, "node id", limits) }
            validateObject(node.props, "node props", limits)
            validateConditions(node.rules, limits)
            validateConditions(node.visibleWhen, limits)
            validateConditions(node.errorWhen, limits)
            node.action?.let { validateAction(it, limits) }
            node.onAppear?.let { validateAction(it, limits) }
            node.onDisappear?.let { validateAction(it, limits) }
            node.semantics?.let {
                it.contentDescription?.let { value -> validateString(value, "content description", limits) }
                it.role?.let { value -> validateString(value, "semantics role", limits) }
                it.liveRegion?.let { value -> validateString(value, "live region", limits) }
            }

            if (node.children.size > limits.maxListItems) {
                fail("Child count exceeds ${limits.maxListItems}")
            }
            node.fallback?.let { pending += it to depth + 1 }
            node.children.forEach { pending += it to depth + 1 }
        }
    }

    private fun validateAction(root: UiAction, limits: SduiPayloadLimits) {
        val pending = mutableListOf(root to 1)
        while (pending.isNotEmpty()) {
            val (action, depth) = pending.removeAt(pending.lastIndex)
            if (depth > limits.maxActionDepth) fail("Action depth exceeds ${limits.maxActionDepth}")
            validateString(action.type, "action type", limits)
            action.target?.let { validateString(it, "action target", limits) }
            action.method?.let { validateString(it, "action method", limits) }
            action.body?.let { validateObject(it, "action body", limits) }
            validateObject(action.metadata, "action metadata", limits)
            when (val feedback = action.feedback) {
                is Feedback.Haptic -> validateString(feedback.intensity, "haptic intensity", limits)
                is Feedback.Sound -> validateString(feedback.name, "sound name", limits)
                null -> Unit
            }
            action.onSuccess?.let { pending += it to depth + 1 }
            action.onError?.let { pending += it to depth + 1 }
        }
    }

    private fun validateConditions(conditions: List<Condition>, limits: SduiPayloadLimits) {
        if (conditions.size > limits.maxListItems) fail("Condition count exceeds ${limits.maxListItems}")
        val pending = conditions.map { it to 1 }.toMutableList()
        while (pending.isNotEmpty()) {
            val (condition, depth) = pending.removeAt(pending.lastIndex)
            if (depth > limits.maxConditionDepth) {
                fail("Condition depth exceeds ${limits.maxConditionDepth}")
            }
            when (condition) {
                is Condition.Equals -> {
                    validateString(condition.field, "condition field", limits)
                    validateValue(condition.value, limits)
                }
                is Condition.NotEmpty -> validateString(condition.field, "condition field", limits)
                is Condition.IsTrue -> validateString(condition.field, "condition field", limits)
                is Condition.Matches -> {
                    validateString(condition.field, "condition field", limits)
                    validateString(condition.regex, "condition regex", limits)
                }
                is Condition.Not -> pending += condition.condition to depth + 1
                is Condition.And -> {
                    if (condition.conditions.size > limits.maxListItems) fail("Condition count exceeds ${limits.maxListItems}")
                    condition.conditions.forEach { pending += it to depth + 1 }
                }
                is Condition.Or -> {
                    if (condition.conditions.size > limits.maxListItems) fail("Condition count exceeds ${limits.maxListItems}")
                    condition.conditions.forEach { pending += it to depth + 1 }
                }
                is Condition.Script -> validateString(condition.expression, "condition expression", limits)
            }
        }
    }

    private fun validateObject(
        values: Map<String, SduiValue>,
        name: String,
        limits: SduiPayloadLimits
    ) {
        if (values.size > limits.maxObjectEntries) fail("$name exceeds ${limits.maxObjectEntries} entries")
        values.forEach { (key, value) ->
            validateString(key, "$name key", limits)
            validateValue(value, limits)
        }
    }

    private fun validateValue(root: SduiValue, limits: SduiPayloadLimits) {
        val pending = mutableListOf(root to 1)
        while (pending.isNotEmpty()) {
            val (value, depth) = pending.removeAt(pending.lastIndex)
            if (depth > limits.maxValueDepth) fail("Value depth exceeds ${limits.maxValueDepth}")
            when (value) {
                is SduiValue.StringValue -> validateString(value.value, "value", limits)
                is SduiValue.NumberValue, is SduiValue.BooleanValue -> Unit
                is SduiValue.ListValue -> {
                    if (value.value.size > limits.maxListItems) fail("List exceeds ${limits.maxListItems} items")
                    value.value.forEach { pending += it to depth + 1 }
                }
                is SduiValue.ObjectValue -> {
                    if (value.value.size > limits.maxObjectEntries) {
                        fail("Object exceeds ${limits.maxObjectEntries} entries")
                    }
                    value.value.forEach { (key, nested) ->
                        validateString(key, "object key", limits)
                        pending += nested to depth + 1
                    }
                }
            }
        }
    }

    private fun validateString(value: String, name: String, limits: SduiPayloadLimits) {
        if (value.isBlank() && (name == "node type" || name == "action type")) {
            fail("$name must not be blank")
        }
        if (value.length > limits.maxStringLength) {
            fail("$name exceeds ${limits.maxStringLength} characters")
        }
    }

    private fun fail(message: String): Nothing = throw SduiPayloadValidationException(message)
}
