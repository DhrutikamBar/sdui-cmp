package com.example.sdui.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SduiTest {

    @Test
    fun testSduiValueSerialization() {
        val stringVal = SduiValue.StringValue("hello")
        assertEquals("\"hello\"", Json.encodeToString<SduiValue>(stringVal))

        val numberVal = SduiValue.NumberValue(42.0)
        assertEquals("42.0", Json.encodeToString<SduiValue>(numberVal))

        val booleanVal = SduiValue.BooleanValue(true)
        assertEquals("true", Json.encodeToString<SduiValue>(booleanVal))

        val objVal = SduiValue.ObjectValue(mapOf("key" to SduiValue.StringValue("value")))
        assertEquals("{\"key\":\"value\"}", Json.encodeToString<SduiValue>(objVal))
    }

    @Test
    fun decodesLegacyNodeAsCurrentDocument() {
        val document = SduiDocumentCodec.decode("""{\"type\":\"text\"}""")

        assertEquals(CURRENT_SDUI_SCHEMA_VERSION, document.schemaVersion)
        assertEquals("text", document.root.type)
    }

    @Test
    fun decodesVersionedDocument() {
        val document = SduiDocumentCodec.decode(
            """{\"schemaVersion\":1,\"root\":{\"type\":\"text\"}}"""
        )

        assertEquals("text", document.root.type)
    }

    @Test
    fun rejectsUnsupportedDocumentVersion() {
        val error = kotlin.test.assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.decode("""{\"schemaVersion\":2,\"root\":{\"type\":\"text\"}}""")
        }

        assertTrue(error.message.orEmpty().contains("Unsupported SDUI schema version"))
    }

    @Test
    fun testUiNodeSerialization() {
        val node = UiNode(
            type = "text",
            props = mapOf("value" to SduiValue.StringValue("Hello"))
        )
        val json = Json.encodeToString(node)
        assertTrue(json.contains("\"type\":\"text\""))
        assertTrue(json.contains("\"props\":{\"value\":\"Hello\"}"))
    }
}
