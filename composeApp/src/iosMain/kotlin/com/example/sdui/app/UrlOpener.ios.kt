package com.example.sdui.app

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberUrlOpener(): (String) -> Unit {
    return { url ->
        NSURL.URLWithString(url)?.let { nsUrl -> UIApplication.sharedApplication.openURL(nsUrl) }
    }
}