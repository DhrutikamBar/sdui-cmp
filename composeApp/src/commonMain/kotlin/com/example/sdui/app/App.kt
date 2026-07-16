package com.example.sdui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.sdui.shared.UiNode

/**
 * baseUrl == null (the default) renders a LocalScreens.* value — no server needed.
 * Pass a baseUrl to fetch from the Ktor server instead, once you want the real round trip.
 * See MainActivity.kt / MainViewController.kt for the platform-specific URL notes.
 */
@Composable
fun App(baseUrl: String? = null) {
    val registry = remember { ComponentRegistry().apply { registerCoreWidgets() } }
    val formState = remember { FormState() }
    var screen by remember { mutableStateOf<UiNode?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(baseUrl, retryTrigger) {
        loadError = null
        screen = try {
            if (baseUrl != null) {
                UiRepository(baseUrl).fetchScreen("/api/ui/home")
            } else {
                decodeLocalScreen(LocalScreens.home)
            }
        } catch (e: Exception) {
            loadError = e.message ?: "Something went wrong"
            null
        }
    }

    val actions = ActionHandler { action ->
        when (action.type) {
            "toggleState" -> action.target?.let { key ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                formState[key] = if (formState[key] == "true") "false" else "true"
            }
            else -> println("Action fired: ${action.type} -> ${action.target}")
        }
    }

    MaterialTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                CompositionLocalProvider(LocalSnackBarHostState provides snackbarHostState) {
                    val currentScreen = screen
                    val error = loadError
                    when {
                        currentScreen != null -> registry.Render(currentScreen, actions, formState)
                        error != null -> ErrorState(message = error, onRetry = { retryTrigger++ })
                        else -> LoadingSkeleton()
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSkeleton() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ShimmerBox(Modifier.fillMaxWidth().height(180.dp), cornerRadius = 12)
        Spacer(Modifier.height(12.dp))
        repeat(3) {
            ShimmerBox(Modifier.fillMaxWidth().height(20.dp))
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Native, not JSON-driven — there's no schema to describe this with, since fetching
 * the schema itself is what failed. A real app would swap the icon/copy for its own tone.
 */
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val warningIcon = materialIcon("warning")
        if (warningIcon != null) {
            Icon(imageVector = warningIcon, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))
        Text("Couldn't load this screen", style = MaterialTheme.typography.titleMedium)
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}