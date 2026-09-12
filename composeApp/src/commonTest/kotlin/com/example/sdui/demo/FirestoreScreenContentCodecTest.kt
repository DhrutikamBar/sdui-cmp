package com.example.sdui.demo

import com.example.sdui.shared.SduiPayloadValidationException
import com.example.sdui.shared.SduiValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FirestoreScreenContentCodecTest {
    @Test
    fun decodesAConsoleFriendlyJsonString() {
        val screen = FirestoreScreenContentCodec.decode(
            """{"schemaVersion":1,"root":{"type":"text","props":{"value":"Hello"}}}"""
        )

        assertEquals("text", screen.type)
    }

    @Test
    fun decodesANativeFirestoreMap() {
        val screen = FirestoreScreenContentCodec.decode(
            mapOf(
                "schemaVersion" to 1L,
                "root" to mapOf(
                    "type" to "text",
                    "props" to mapOf("value" to "Hello")
                )
            )
        )

        assertEquals("text", screen.type)
        assertEquals(SduiValue.StringValue("Hello"), screen.props["value"])
    }

    @Test
    fun rejectsInvalidFirestoreDocumentContent() {
        assertFailsWith<SduiPayloadValidationException> {
            FirestoreScreenContentCodec.decode(
                """{"schemaVersion":99,"root":{"type":"text"}}"""
            )
        }
    }

    @Test
    fun rejectsUnsupportedFirestoreValues() {
        assertFailsWith<IllegalArgumentException> {
            FirestoreScreenContentCodec.decode(
                mapOf("type" to "text", "unsupported" to Any())
            )
        }
    }
}
