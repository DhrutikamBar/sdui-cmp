package com.dhruti.sdui.sdk

import androidx.compose.runtime.Composable

@Composable
expect fun rememberUrlOpener(): (String) -> Unit
