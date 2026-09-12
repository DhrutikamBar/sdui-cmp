package com.example.sdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.example.sdui.app.App
import com.example.sdui.app.DemoSduiActionPolicy

/**
 * Reference host controlled by Firestore screen documents on Android.
 *
 * The renderer SDK remains independent of Firebase. On iOS, bundled documents
 * remain active until a Firebase iOS host configuration is supplied.
 */
@Composable
fun DemoApp() {
    val screenSource = remember { createFirebaseScreenSource() }
    DisposableEffect(screenSource) {
        onDispose(screenSource::close)
    }

    App(
        screenSource = screenSource,
        apiCallClient = LocalDemoApiCallClient,
        actionPolicy = DemoSduiActionPolicy
    )
}
