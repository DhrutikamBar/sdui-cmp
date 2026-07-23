package com.example.sdui.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.sdui.shared.Feedback
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import com.example.sdui.shared.UiAction
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch

/**
 * Example interceptor for analytics.
 */
class AnalyticsInterceptor(private val reporter: ReportingService) : ActionInterceptor {
    override fun intercept(action: UiAction, next: (UiAction) -> Unit) {
        val metadata = action.metadata.mapValues { it.value.toString() }.toMutableMap()
        metadata["action_type"] = action.type
        action.target?.let { metadata["target"] = it }

        reporter.reportEvent("action_fired", metadata)
        next(action)
    }
}

/**
 * Handles haptic and sound feedback for actions.
 */
class FeedbackInterceptor(private val haptics: androidx.compose.ui.hapticfeedback.HapticFeedback) : ActionInterceptor {
    override fun intercept(action: UiAction, next: (UiAction) -> Unit) {
        action.feedback?.let { fb ->
            if (fb is Feedback.Haptic) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
        next(action)
    }
}

/**
 * Supabase is now the only screen source — no local fallback, no separate Ktor server URL.
 * supabaseUrl / supabaseKey are required, not optional, since there's nowhere else to fall back to.
 */
@Composable
fun App(
    supabaseUrl: String, 
    supabaseKey: String,
    driverFactory: DatabaseDriverFactory
) {
    val registry = remember { ComponentRegistry().apply { registerCoreWidgets() } }
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val repository = remember(supabaseUrl, supabaseKey) { 
        SupaBaseUiRepository(supabaseUrl, supabaseKey, driverFactory) 
    }
    val reporter = remember { ConsoleReportingService() }
    val resourceResolver = rememberResourceResolver()

    var designTokens by remember { mutableStateOf(DesignTokens()) }

    LaunchedEffect(supabaseUrl) {
        try {
            // In a real app, this would be a specific endpoint/table for tokens
            // designTokens = repository.fetchTokens()
        } catch (e: Exception) { }
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalReportingService provides reporter,
            LocalResourceResolver provides resourceResolver,
            LocalDesignTokens provides designTokens
        ) {
            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    CompositionLocalProvider(LocalSnackBarHostState provides snackbarHostState) {
                    NavHost(navController = navController, startDestination = SduiScreen("home")) {
                        composable<SduiScreen> { backStackEntry ->
                            val route: SduiScreen = backStackEntry.toRoute()
                            SduiScreenContent(
                                path = route.path,
                                repository = repository,
                                supabaseUrl = supabaseUrl,
                                supabaseKey = supabaseKey,
                                registry = registry,
                                navController = navController
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun SduiScreenContent(
    path: String,
    repository: SupaBaseUiRepository,
    supabaseUrl: String,
    supabaseKey: String,
    registry: ComponentRegistry,
    navController: NavHostController
) {
    val formState = rememberSaveable(saver = FormState.Saver) { FormState() }
    var screen by remember { mutableStateOf<UiNode?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val openUrl = rememberUrlOpener()
    val scope = rememberCoroutineScope()
    val reporter = LocalReportingService.current

    LaunchedEffect(path, retryTrigger) {
        loadError = null
        screen = try {
            val fetched = try {
                repository.fetchScreen(path)
            } catch (e: Exception) {
                // If Supabase fetch fails, try local fallback for better DX
                val localJsonStr = when (path) {
                    "home" -> LocalScreens.home
                    "welcome" -> LocalScreens.welcome
                    "wallet" -> LocalScreens.wallet
                    "checkout" -> LocalScreens.checkout
                    else -> null
                }
                if (localJsonStr != null) decodeLocalScreen(localJsonStr) else throw e
            }
            
            reporter.reportEvent("screen_view", mapOf("path" to path))
            
            // Predictive prefetching: fetch next screens in the background
            UiScanner.findNavigablePaths(fetched).forEach { nextPath ->
                repository.prefetch(nextPath)
            }
            
            fetched
        } catch (e: Exception) {
            val context = mapOf("path" to path, "error" to (e.message ?: "unknown"))
            reporter.reportCrash(e, context)
            loadError = e.message ?: "Something went wrong"
            null
        }
    }

    val actionRegistry = remember(navController, reporter) {
        lateinit var registryRef: ActionRegistry
        val registry = ActionRegistry(
            interceptors = listOf(
                AnalyticsInterceptor(reporter),
                FeedbackInterceptor(haptics)
            )
        ).apply {
            register("navigate") { action -> action.target?.let { navController.navigate(SduiScreen(it)) } }
            register("back") { navController.popBackStack() }
            register("openUrl") { action -> action.target?.let(openUrl) }
            register("toggleState") { action ->
                action.target?.let { key ->
                    val current = formState[key] as? SduiValue.BooleanValue
                    formState[key] = SduiValue.BooleanValue(!(current?.value ?: false))
                }
            }
            register("apiCall") { action ->
                val url = action.target ?: return@register
                scope.launch {
                    try {
                        val response = repository.httpClient.request(supabaseUrl + url) {
                            method = HttpMethod.parse(action.method ?: "POST")
                            header("apikey", supabaseKey)
                            header("Authorization", "Bearer $supabaseKey")
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

    when {
        screen != null -> registry.RenderRoot(screen!!, actions, formState)
        loadError != null -> ErrorState(message = loadError!!, onRetry = { retryTrigger++ })
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
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}