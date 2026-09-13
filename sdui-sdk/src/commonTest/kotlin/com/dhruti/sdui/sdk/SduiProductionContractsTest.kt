package com.dhruti.sdui.sdk

import com.example.sdui.shared.SduiAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SduiProductionContractsTest {
    @Test
    fun capabilitySnapshotUsesRegisteredWidgetsAndHostActions() {
        val registry = ComponentRegistry().apply {
            register("hostCard") { _, _, _ -> }
        }

        val capabilities = registry.capabilities(setOf("navigate", "analytics"))

        assertEquals(SDK_VERSION, capabilities.sdkVersion)
        assertTrue("hostCard" in capabilities.widgetTypes)
        assertEquals(setOf("navigate", "analytics"), capabilities.actionTypes)
    }

    @Test
    fun strictResourcePolicyAllowsOnlyConfiguredHttpsHost() {
        val policy = HostAllowlistResourcePolicy(setOf("cdn.example.com"))

        assertTrue(policy.allows(SduiRemoteResourceType.IMAGE, "https://cdn.example.com/image.png"))
        assertTrue(!policy.allows(SduiRemoteResourceType.LOTTIE, "http://cdn.example.com/file.json"))
        assertTrue(!policy.allows(SduiRemoteResourceType.IMAGE, "https://other.example.com/image.png"))
    }

    @Test
    fun actionAndDataStatesRemainStructured() {
        assertIs<SduiDataState.Loading>(SduiDataState.Loading)
        assertIs<SduiActionResult.ValidationFailure>(
            SduiActionResult.ValidationFailure(mapOf("email" to "Invalid"))
        )
    }
}
