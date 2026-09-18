package com.example.sdui.app

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dhruti.sdui.sdk.*
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.TimeSource

/**
 * Demo navigation shell for the reference application.
 *
 * It preserves the existing sample navigation while delegating screen rendering
 * to [SduiReferenceScreenHost], whose dependencies are fully host-provided.
 */
@Composable
fun App(
    screenSource: ScreenSource,
    apiCallClient: SduiApiCallClient,
    actionPolicy: SduiActionPolicy = DemoSduiActionPolicy,
    dataProvider: SduiScreenDataProvider = EmptySduiScreenDataProvider
) {
    val componentRegistry = remember {
        ComponentRegistry().apply {
            registerCoreWidgets()
            register("nativeSlot") { node, _, _ ->
                if (node.props["id"].asString() == "balanceToggle") {
                    BalanceToggle(node)
                }
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val navigator = remember(navController) {
        object : SduiNavigator {
            override fun navigate(path: String) {
                navController.navigate(SduiScreen(path))
            }

            override fun goBack() {
                navController.popBackStack()
            }
        }
    }
    val openUrl = rememberUrlOpener()
    val urlHandler = remember(openUrl) { SduiUrlHandler(openUrl) }
    val reportingService = remember { ConsoleReportingService() }
    val resourceResolver = rememberResourceResolver()
    val designTokens = remember { DesignTokens() }

    MaterialTheme {
        CompositionLocalProvider(LocalSnackBarHostState provides snackbarHostState) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Surface(
                    modifier = Modifier.padding(padding),
                    color = resolveColor("brand-primary") ?: MaterialTheme.colorScheme.surface
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = SduiScreen("home"),
                        enterTransition = { fadeIn(tween(220)) },
                        exitTransition = { fadeOut(tween(160)) },
                        popEnterTransition = { fadeIn(tween(220)) },
                        popExitTransition = { fadeOut(tween(160)) }
                    ) {
                        composable<SduiScreen> { backStackEntry ->
                            val route: SduiScreen = backStackEntry.toRoute()
                            SduiReferenceScreenHost(
                                path = route.path,
                                screenSource = screenSource,
                                apiCallClient = apiCallClient,
                                dataProvider = dataProvider,
                                componentRegistry = componentRegistry,
                                navigator = navigator,
                                urlHandler = urlHandler,
                                actionPolicy = actionPolicy,
                                reportingService = reportingService,
                                resourceResolver = resourceResolver,
                                designTokens = designTokens,
                                supportedActionTypes = demoSupportedActionTypes
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Injectable reference screen host.
 *
 * Hosts own navigation, URL handling, component registration, screen delivery,
 * API execution, action policy, reporting, resources, and design tokens. This
 * composable deliberately does not select a navigation library or HTTP client.
 */
@Composable
fun SduiReferenceScreenHost(
    path: String,
    screenSource: ScreenSource,
    apiCallClient: SduiApiCallClient,
    dataProvider: SduiScreenDataProvider = EmptySduiScreenDataProvider,
    componentRegistry: ComponentRegistry,
    navigator: SduiNavigator,
    urlHandler: SduiUrlHandler,
    actionPolicy: SduiActionPolicy,
    reportingService: ReportingService,
    resourceResolver: ResourceResolver,
    designTokens: DesignTokens = DesignTokens(),
    supportedActionTypes: Set<String> = emptySet(),
    dataContext: SduiDataContext = SduiDataContext()
) {
    CompositionLocalProvider(
        LocalReportingService provides reportingService,
        LocalResourceResolver provides resourceResolver,
        LocalDesignTokens provides designTokens
    ) {
        SduiScreenContent(
            path = path,
            screenSource = screenSource,
            apiCallClient = apiCallClient,
            dataProvider = dataProvider,
            registry = componentRegistry,
            navigator = navigator,
            urlHandler = urlHandler,
            actionPolicy = actionPolicy,
            supportedActionTypes = supportedActionTypes,
            dataContext = dataContext
        )
    }
}

@Composable
private fun SduiScreenContent(
    path: String,
    screenSource: ScreenSource,
    apiCallClient: SduiApiCallClient,
    dataProvider: SduiScreenDataProvider,
    registry: ComponentRegistry,
    navigator: SduiNavigator,
    urlHandler: SduiUrlHandler,
    actionPolicy: SduiActionPolicy,
    supportedActionTypes: Set<String>,
    dataContext: SduiDataContext
) {
    val formState = rememberSaveable(saver = FormState.Saver) { FormState() }
    var screen by remember { mutableStateOf<UiNode?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val reporter = LocalReportingService.current
    val dataState by dataProvider.stateFor(path).collectAsState()

    LaunchedEffect(path, retryTrigger) {
        loadError = null
        val startedAt = TimeSource.Monotonic.markNow()
        var loadSource = ScreenLoadSource.UNKNOWN
        screen = try {
            val fetched = try {
                val result = withTimeoutOrNull(SCREEN_LOAD_TIMEOUT_MILLIS) {
                    screenSource.loadScreen(
                        ScreenRequest(path, forceRefresh = retryTrigger > 0)
                    )
                } ?: throw IllegalStateException("Timed out loading screen: $path")
                when (result) {
                    is ScreenLoadResult.Success -> {
                        loadSource = result.source
                        result.screen
                    }
                    is ScreenLoadResult.Failure -> throw result.cause
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                // If the configured screen source fails, try local fallback for better DX
                println("SDUI: Screen source failed for $path, trying local fallback...")
                val localJsonStr = when (path) {
                    "home" -> LocalScreens.home
                    "welcome" -> LocalScreens.welcome
                    "wallet" -> LocalScreens.wallet
                    "checkout" -> LocalScreens.checkout
                    "send" -> LocalScreens.send
                    "send-success" -> LocalScreens.sendSuccess
                    "settings" -> LocalScreens.settings
                    "order-confirmed" -> LocalScreens.orderConfirmed
                    "lottie-test" -> LocalScreens.lottieTest
            "ui-certification" -> LocalScreens.uiCertification
                    else -> null
                }
                if (localJsonStr != null) {
                    println("KTOR: Using local fallback for $path")
                    decodeLocalScreen(localJsonStr)
                } else {
                    throw e
                }
            }
            
            SduiSemanticValidator.validate(
                root = fetched,
                isWidgetSupported = registry::supports,
                isActionSupported = { type ->
                    supportedActionTypes.isEmpty() || type in supportedActionTypes
                }
            )

            reporter.reportEvent(
                "screen_view",
                mapOf(
                    "path" to path,
                    "source" to loadSource.name.lowercase(),
                    "load_ms" to startedAt.elapsedNow().inWholeMilliseconds.toString()
                )
            )
            
            // Predictive prefetching: fetch next screens in the background
            UiScanner.findNavigablePaths(fetched).forEach { nextPath ->
                screenSource.prefetch(nextPath)
            }
            
            fetched
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            val context = mapOf("path" to path, "error" to (e.message ?: "unknown"))
            reporter.reportCrash(e, context)
            loadError = e.message ?: "Something went wrong"
            null
        }
    }

    val actionRegistry = remember(navigator, urlHandler, apiCallClient, dataProvider, path, actionPolicy, reporter, haptics, formState, scope) {
        lateinit var registryRef: ActionRegistry
        val registry = ActionRegistry(
            interceptors = listOf(
                AnalyticsInterceptor(reporter),
                FeedbackInterceptor(haptics)
            ),
            actionPolicy = actionPolicy
        ).apply {
            register("navigate") { action -> action.target?.let(navigator::navigate) }
            register("back") { navigator.goBack() }
            register("analytics") { action ->
                action.target?.let { eventName -> reporter.reportEvent(eventName) }
            }
            register("refreshData") {
                scope.launch { dataProvider.refresh(path) }
            }
            register("openUrl") { action -> action.target?.let(urlHandler::open) }
            register("toggleState") { action ->
                action.target?.let { key ->
                    val current = formState[key] as? SduiValue.BooleanValue
                    formState[key] = SduiValue.BooleanValue(!(current?.value ?: false))
                }
            }
            register("apiCall") { action ->
                scope.launch {
                    val stateKey = (action.metadata["stateKey"] as? SduiValue.StringValue)?.value
                    stateKey?.let { formState[it] = SduiValue.BooleanValue(true) }
                    try {
                        if (apiCallClient.execute(action, formState)) {
                            reporter.reportEvent("action_result", mapOf("type" to "apiCall", "result" to "success"))
                            action.onSuccess?.let { registryRef.dispatch(it) }
                        } else {
                            reporter.reportEvent("action_result", mapOf("type" to "apiCall", "result" to "failure"))
                            action.onError?.let { registryRef.dispatch(it) }
                        }
                    } catch (cancellation: CancellationException) {
                        reporter.reportEvent("action_result", mapOf("type" to "apiCall", "result" to "cancelled"))
                        throw cancellation
                    } catch (e: Exception) {
                        reporter.reportEvent(
                            "action_result",
                            mapOf("type" to "apiCall", "result" to "failure", "error" to (e.message ?: "unknown"))
                        )
                        action.onError?.let { registryRef.dispatch(it) }
                    } finally {
                        stateKey?.let { formState[it] = SduiValue.BooleanValue(false) }
                    }
                }
            }
        }
        registryRef = registry
        registry
    }
    val actions = ActionHandler { action -> actionRegistry.dispatch(action) }

    when {
        loadError != null -> ErrorState(message = loadError!!, onRetry = { retryTrigger++ })
        screen == null -> LoadingSkeleton()
        dataState is SduiDataState.Loading -> LoadingSkeleton()
        dataState is SduiDataState.Empty -> EmptyDataState()
        dataState is SduiDataState.Failure -> {
            val failure = dataState as SduiDataState.Failure
            ErrorState(
                message = failure.message,
                onRetry = {
                    if (failure.retryable) {
                        scope.launch { dataProvider.refresh(path) }
                    }
                }
            )
        }
        dataState is SduiDataState.Content<*> -> {
            val liveContext = (dataState as SduiDataState.Content<SduiDataContext>).value
            SduiRenderer(
                screen!!,
                actions,
                formState = formState,
                registry = registry,
                dataContext = dataContext.withValues(liveContext.values)
            )
        }
    }
}

@Composable
private fun EmptyDataState() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nothing to show yet", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "This screen has no available data.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
fun ErrorState(message: String, onRetry: () -> Unit) {
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

private const val SCREEN_LOAD_TIMEOUT_MILLIS = 15_000L

private val demoSupportedActionTypes = setOf(
    "navigate",
    "back",
    "analytics",
    "refreshData",
    "openUrl",
    "toggleState",
    "apiCall"
)
