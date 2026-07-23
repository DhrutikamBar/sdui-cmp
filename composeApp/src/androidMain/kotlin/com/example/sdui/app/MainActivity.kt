package com.example.sdui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dhruti.sdui.sdk.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 10.0.2.2 is the Android emulator's alias for your host machine's localhost,
            // where `./gradlew :server:run` is listening on port 8080.
            App(
                supabaseUrl = SduiConfig.supabaseUrl, 
                supabaseKey = SduiConfig.supabaseKey,
                driverFactory = DatabaseDriverFactory(this)
            )
        }
    }
}
