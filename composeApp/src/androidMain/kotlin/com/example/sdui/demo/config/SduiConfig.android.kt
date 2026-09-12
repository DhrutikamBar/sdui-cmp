package com.example.sdui.demo.config

import com.example.sdui.app.BuildConfig

actual object SduiConfig {
    actual val supabaseUrl: String = BuildConfig.SUPABASE_URL
    actual val supabaseKey: String = BuildConfig.SUPABASE_KEY
}
