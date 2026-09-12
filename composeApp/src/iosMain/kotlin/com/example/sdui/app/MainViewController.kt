package com.example.sdui.app

import androidx.compose.ui.window.ComposeUIViewController
import com.example.sdui.demo.DemoApp
import com.example.sdui.demo.config.SduiConfig
import com.example.sdui.demo.data.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController {
    DemoApp(
        supabaseUrl = SduiConfig.supabaseUrl, 
        supabaseKey = SduiConfig.supabaseKey,
        driverFactory = DatabaseDriverFactory()
    )
}
