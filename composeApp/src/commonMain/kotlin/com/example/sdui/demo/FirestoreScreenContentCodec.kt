package com.example.sdui.demo

import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Converts a Firestore content value into a validated SDUI screen.
 *
 * The console-friendly String form and Firestore Map form are both accepted.
 */
object FirestoreScreenContentCodec {
    fun decode(content: Any): UiNode = when (content) {
        is String -> SduiDocumentCodec.decode(content).root
        else -> SduiDocumentCodec.decode(content.toJsonElement()).root
    }
}

private fun Any.toJsonElement(): JsonElement = when (this) {
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Int -> JsonPrimitive(this)
    is Long -> JsonPrimitive(this)
    is Float -> JsonPrimitive(this)
    is Double -> JsonPrimitive(this)
    is Map<*, *> -> JsonObject(
        entries.associate { (key, value) ->
            require(key is String) { "Firestore SDUI object keys must be strings" }
            key to (value?.toJsonElement() ?: JsonNull)
        }
    )
    is List<*> -> JsonArray(map { it?.toJsonElement() ?: JsonNull })
    else -> throw IllegalArgumentException(
        "Unsupported Firestore SDUI value: " + this::class.simpleName
    )
}
