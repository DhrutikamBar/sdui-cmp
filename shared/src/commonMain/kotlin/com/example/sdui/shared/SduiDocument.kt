package com.example.sdui.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/** The currently supported wire format for an SDUI document. */
const val CURRENT_SDUI_SCHEMA_VERSION: Int = 1

/**
 * Versioned SDUI payload.
 *
 * `root` contains the render tree. The optional document envelope lets the
 * protocol evolve without changing [UiNode] itself.
 */
@Serializable
data class SduiDocument(
    val schemaVersion: Int = CURRENT_SDUI_SCHEMA_VERSION,
    val root: UiNode
)

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

    fun decode(payload: String): SduiDocument = decode(json.parseToJsonElement(payload))

    fun decode(element: JsonElement): SduiDocument {
        val document = if (element is JsonObject && "root" in element) {
            json.decodeFromJsonElement<SduiDocument>(element)
        } else {
            SduiDocument(root = json.decodeFromJsonElement<UiNode>(element))
        }
        return validate(document)
    }

    fun encode(document: SduiDocument): String =
        json.encodeToString(validate(document))

    fun fromLegacy(root: UiNode): SduiDocument =
        validate(SduiDocument(root = root))

    fun validate(document: SduiDocument): SduiDocument {
        if (document.schemaVersion != CURRENT_SDUI_SCHEMA_VERSION) {
            throw SduiPayloadValidationException(
                "Unsupported SDUI schema version: ${document.schemaVersion}"
            )
        }
        if (document.root.type.isBlank()) {
            throw SduiPayloadValidationException("SDUI root type must not be blank")
        }
        return document
    }
}
