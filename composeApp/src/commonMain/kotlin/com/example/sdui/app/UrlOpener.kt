package com.example.sdui.app


import androidx.compose.runtime.Composable

/** Returns a function that opens a URL in the system browser. Platform-specific because
 *  Android needs a Context (via LocalContext, Android-only) and iOS needs UIApplication. */
@Composable
expect fun rememberUrlOpener(): (String) -> Unit