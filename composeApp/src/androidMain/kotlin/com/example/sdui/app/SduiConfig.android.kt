package com.example.sdui.app

actual object SduiConfig {
    actual val supabaseUrl: String = BuildConfig.SUPABASE_URL
    actual val supabaseKey: String = BuildConfig.SUPABASE_KEY
}
