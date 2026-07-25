package com.example.sdui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                supabaseUrl = SduiConfig.supabaseUrl, 
                supabaseKey = SduiConfig.supabaseKey,
                driverFactory = DatabaseDriverFactory(this)
            )
        }
    }
}
