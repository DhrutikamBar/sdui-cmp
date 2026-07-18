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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sdui.shared.UiNode
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch

/**
 * baseUrl == null (the default) renders from LocalScreens — no server needed.
 * Pass a baseUrl to fetch from the Ktor server instead, once you want the real round trip.
 * See MainActivity.kt / MainViewController.kt for the platform-specific URL notes.
 */
@Composable
fun App(baseUrl: String? = null) {
    val registry = remember { ComponentRegistry().apply { registerCoreWidgets() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    MaterialTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                CompositionLocalProvider(LocalSnackBarHostState provides snackbarHostState) {
                    NavHost(navController = navController, startDestination = SduiScreen("/api/ui/home")) {
                        composable<SduiScreen> { backStackEntry ->
                            val route: SduiScreen = backStackEntry.toRoute()
                            SduiScreenContent(path = route.path, baseUrl = baseUrl, registry = registry, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

/**
 * One screen's worth of state — fresh per backstack entry, since Navigation Compose scopes
 * composition (and therefore `remember`) to each pushed entry independently.
 */
@Composable
private fun SduiScreenContent(
    path: String,
    baseUrl: String?,
    registry: ComponentRegistry,
    navController: NavHostController
) {
    val formState = remember { FormState() }
    var screen by remember { mutableStateOf<UiNode?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val openUrl = rememberUrlOpener()
    val scope = rememberCoroutineScope()
    val httpClient = remember { HttpClient { install(ContentNegotiation) { json() } } }

    LaunchedEffect(path, retryTrigger) {
        loadError = null
        screen = try {
            if (baseUrl != null) {
                // Real SDUI routing: whatever path the action carried is the literal URL
                // fetched. The client has no table of valid paths — the backend decides
                // entirely, including what "path" even means.
                UiRepository(baseUrl).fetchScreen(path)
            } else {
                // No backend to ask in local mode, so there's nothing to route — this is a
                // fixed preview, not a stand-in for routing. Change LocalScreens.checkout
                // below to preview a different screen; `path` is intentionally unused here.
                decodeLocalScreen(LocalScreens.checkout)
            }
        } catch (e: Exception) {
            loadError = e.message ?: "Something went wrong"
            null
        }
    }

    val actionRegistry = remember(navController) {
        // registryRef exists so "apiCall" can dispatch its own onSuccess/onError back through
        // the same registry — declared before construction since the reference is only
        // actually used later, when apiCall fires, by which point it's assigned below.
        lateinit var registryRef: ActionRegistry
        val registry = ActionRegistry().apply {
            register("navigate") { action -> action.target?.let { navController.navigate(SduiScreen(it)) } }
            register("back") { navController.popBackStack() }
            register("openUrl") { action -> action.target?.let(openUrl) }
            register("toggleState") { action ->
                action.target?.let { key ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    formState[key] = if (formState[key] == "true") "false" else "true"
                }
            }
            register("apiCall") { action ->
                val url = action.target
                if (url == null) return@register
                if (baseUrl == null) {
                    action.onSuccess?.let { registryRef.dispatch(it) }
                    return@register
                }
                scope.launch {
                    try {
                        val response = httpClient.request(baseUrl + url) {
                            method = HttpMethod.parse(action.method ?: "POST")
                            action.body?.let { body ->
                                contentType(ContentType.Application.Json)
                                setBody(interpolate(body, formState))
                            }
                        }
                        if (response.status.isSuccess()) {
                            action.onSuccess?.let { registryRef.dispatch(it) }
                        } else {
                            action.onError?.let { registryRef.dispatch(it) }
                        }
                    } catch (e: Exception) {
                        action.onError?.let { registryRef.dispatch(it) }
                    }
                }
            }
        }
        registryRef = registry
        registry
    }
    val actions = ActionHandler { action -> actionRegistry.dispatch(action) }

    val currentScreen = screen
    val error = loadError
    when {
        currentScreen != null -> registry.Render(currentScreen, actions, formState)
        error != null -> ErrorState(message = error, onRetry = { retryTrigger++ })
        else -> LoadingSkeleton()
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