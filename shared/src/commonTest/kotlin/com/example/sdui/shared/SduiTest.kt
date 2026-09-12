package com.example.sdui.shared

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
        val document = SduiDocumentCodec.decode("""{"type":"text"}""")

        assertEquals(CURRENT_SDUI_SCHEMA_VERSION, document.schemaVersion)
        assertEquals("text", document.root.type)
    }

    @Test
    fun decodesVersionedDocument() {
        val document = SduiDocumentCodec.decode(
            """{"schemaVersion":1,"root":{"type":"text"}}"""
        )

        assertEquals("text", document.root.type)
    }

    @Test
    fun rejectsUnsupportedDocumentVersion() {
        val error = assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.decode("""{"schemaVersion":2,"root":{"type":"text"}}""")
        }

        assertTrue(error.message.orEmpty().contains("Unsupported SDUI schema version"))
    }

    @Test
    fun rejectsPayloadAboveConfiguredSize() {
        assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.decode(
                """{"type":"text"}""",
                SduiPayloadLimits(maxPayloadChars = 4)
            )
        }
    }

    @Test
    fun rejectsDeepNodeTree() {
        val document = SduiDocument(
            root = UiNode(type = "root", children = listOf(UiNode(type = "child")))
        )

        assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.validate(document, SduiPayloadLimits(maxNodeDepth = 1))
        }
    }

    @Test
    fun rejectsNestedActionsAboveConfiguredDepth() {
        val document = SduiDocument(
            root = UiNode(
                type = "button",
                action = UiAction(type = "first", onSuccess = UiAction(type = "second"))
            )
        )

        assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.validate(document, SduiPayloadLimits(maxActionDepth = 1))
        }
    }

    @Test
    fun rejectsOversizedValues() {
        val document = SduiDocument(
            root = UiNode(
                type = "text",
                props = mapOf("value" to SduiValue.StringValue("too long"))
            )
        )

        assertFailsWith<SduiPayloadValidationException> {
            SduiDocumentCodec.validate(document, SduiPayloadLimits(maxStringLength = 3))
        }
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
