package com.example.sdui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle

class IosResourceResolver : ResourceResolver {
    override fun resolveString(key: String): String {
        val cleanKey = key.removePrefix("string-res://")
        return NSBundle.mainBundle.localizedStringForKey(cleanKey, cleanKey, null)
    }

    @Composable
    override fun resolveImage(key: String): Any? {
        val cleanKey = key.removePrefix("res://")
        // In a real iOS KMP app, you'd use Compose resources or a mapping to XCAssets.
        // For this demo, we return the key and let AsyncImage try to handle it.
        return cleanKey
    }
}

@Composable
actual fun rememberResourceResolver(): ResourceResolver {
    return remember { IosResourceResolver() }
}
