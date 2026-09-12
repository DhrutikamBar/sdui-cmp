package com.example.sdui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.dhruti.sdui.sdk.SduiActionPolicy
import com.dhruti.sdui.sdk.SduiApiCallClient
import com.dhruti.sdui.sdk.ScreenSource
import com.example.sdui.app.App
import com.example.sdui.app.DemoSduiActionPolicy
import com.example.sdui.demo.data.DatabaseDriverFactory
import com.example.sdui.demo.data.SupabaseApiCallClient
import com.example.sdui.demo.data.SupabaseScreenSource

/**
 * Reference host controlled by the Supabase screens table.
 *
 * A locally bundled source is used only when Supabase configuration is absent,
 * allowing a newly cloned project to run before its local credentials are added.
 */
@Composable
fun DemoApp(
    supabaseUrl: String,
    supabaseKey: String,
    driverFactory: DatabaseDriverFactory,
    actionPolicy: SduiActionPolicy = DemoSduiActionPolicy
) {
    val configured = isSupabaseConfigured(supabaseUrl, supabaseKey)
    val screenSource: ScreenSource = remember(
        configured,
        supabaseUrl,
        supabaseKey,
        driverFactory
    ) {
        if (configured) {
            SupabaseScreenSource(supabaseUrl, supabaseKey, driverFactory)
        } else {
            LocalDemoScreenSource()
        }
    }
    val apiCallClient: SduiApiCallClient = remember(
        configured,
        supabaseUrl,
        supabaseKey,
        screenSource
    ) {
        if (configured && screenSource is SupabaseScreenSource) {
            SupabaseApiCallClient(screenSource.httpClient, supabaseUrl, supabaseKey)
        } else {
            LocalDemoApiCallClient
        }
    }

    DisposableEffect(screenSource) {
        onDispose(screenSource::close)
    }

    App(
        screenSource = screenSource,
        apiCallClient = apiCallClient,
        actionPolicy = actionPolicy
    )
}

internal fun isSupabaseConfigured(url: String, key: String): Boolean =
    url.isNotBlank() &&
        key.isNotBlank() &&
        url != "null" &&
        key != "null"
