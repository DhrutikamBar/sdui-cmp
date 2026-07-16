package com.example.sdui.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.sdui.shared.UiNode

/**
 * baseUrl differs by platform because "localhost" means different things on each:
 *  - Android emulator: 10.0.2.2 is the special alias for the host machine's localhost.
 *  - iOS simulator: localhost works directly, since the simulator shares the host's network.
 * A real app would inject this instead of hardcoding it per platform.
 */
@Composable
fun App(baseUrl: String) {
    val registry = remember { ComponentRegistry().apply { registerCoreWidgets() } }
    val formState = remember { FormState() }
    val repository = remember { UiRepository(baseUrl) }
    var screen by remember { mutableStateOf<UiNode?>(null) }

    LaunchedEffect(Unit) {
        screen = repository.fetchScreen("/api/ui/home")
    }

    val actions = ActionHandler { action ->
        // A real app wires this to its own navigation graph / analytics.
        println("Action fired: ${action.type} -> ${action.target}")
    }

    MaterialTheme {
        Surface {
            screen?.let { registry.Render(it, actions, formState) }
        }
    }
}
