package com.example.sdui.app

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    App(
        supabaseUrl = SduiConfig.supabaseUrl, 
        supabaseKey = SduiConfig.supabaseKey,
        driverFactory = DatabaseDriverFactory()
    )
}
