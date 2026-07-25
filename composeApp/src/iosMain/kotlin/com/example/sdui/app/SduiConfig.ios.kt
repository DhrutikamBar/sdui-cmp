package com.example.sdui.app

import platform.Foundation.NSBundle

actual object SduiConfig {
    actual val supabaseUrl: String = NSBundle.mainBundle.infoDictionary?.get("SUPABASE_URL") as? String ?: ""
    actual val supabaseKey: String = NSBundle.mainBundle.infoDictionary?.get("SUPABASE_KEY") as? String ?: ""
}
