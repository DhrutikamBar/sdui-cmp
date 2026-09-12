package com.example.sdui.demo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DemoAppConfigurationTest {
    @Test
    fun recognizesCompleteSupabaseConfiguration() {
        assertTrue(
            isSupabaseConfigured(
                url = "https://example.supabase.co",
                key = "public-anon-key"
            )
        )
    }

    @Test
    fun fallsBackWhenBuildPropertiesAreNotConfigured() {
        assertFalse(isSupabaseConfigured("", ""))
        assertFalse(isSupabaseConfigured("null", "null"))
        assertFalse(isSupabaseConfigured("https://example.supabase.co", ""))
    }
}
