package com.example.sdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.sdui.app.App
import com.example.sdui.app.DemoSduiActionPolicy

/** Fully offline reference host using bundled SDUI documents. */
@Composable
fun DemoApp() {
    val screenSource = remember { LocalDemoScreenSource() }
    App(
        screenSource = screenSource,
        apiCallClient = LocalDemoApiCallClient,
        actionPolicy = DemoSduiActionPolicy
    )
}
