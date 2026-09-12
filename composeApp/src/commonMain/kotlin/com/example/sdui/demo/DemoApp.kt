package com.example.sdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.sdui.app.App
import com.example.sdui.app.DemoSduiActionPolicy

/**
 * Reference host that loads Home from Mocki and retains bundled screens as an
 * offline fallback. Other demo screens remain bundled until their endpoints exist.
 */
@Composable
fun DemoApp() {
    val screenSource = remember { MockApiScreenSource() }
    DisposableEffect(screenSource) {
        onDispose(screenSource::close)
    }

    App(
        screenSource = screenSource,
        apiCallClient = LocalDemoApiCallClient,
        actionPolicy = DemoSduiActionPolicy
    )
}
