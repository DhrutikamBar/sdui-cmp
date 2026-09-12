package com.example.sdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.dhruti.sdui.sdk.SduiActionPolicy
import com.example.sdui.app.App
import com.example.sdui.app.DemoSduiActionPolicy
import com.example.sdui.demo.data.DatabaseDriverFactory
import com.example.sdui.demo.data.SupabaseApiCallClient
import com.example.sdui.demo.data.SupabaseScreenSource

/**
 * Assembles the reference host's Supabase-backed dependencies.
 *
 * Production consumers should supply their own [com.dhruti.sdui.sdk.ScreenSource],
 * [com.dhruti.sdui.sdk.SduiApiCallClient], navigation, and policy rather than
 * depending on this demo assembly.
 */
@Composable
fun DemoApp(
    supabaseUrl: String,
    supabaseKey: String,
    driverFactory: DatabaseDriverFactory,
    actionPolicy: SduiActionPolicy = DemoSduiActionPolicy
) {
    val screenSource = remember(supabaseUrl, supabaseKey) {
        SupabaseScreenSource(supabaseUrl, supabaseKey, driverFactory)
    }
    val apiCallClient = remember(screenSource, supabaseUrl, supabaseKey) {
        SupabaseApiCallClient(screenSource.httpClient, supabaseUrl, supabaseKey)
    }

    DisposableEffect(screenSource) {
        onDispose { screenSource.close() }
    }

    App(
        screenSource = screenSource,
        apiCallClient = apiCallClient,
        actionPolicy = actionPolicy
    )
}
