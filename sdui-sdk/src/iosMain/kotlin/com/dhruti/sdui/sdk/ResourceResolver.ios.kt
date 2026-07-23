package com.dhruti.sdui.sdk

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
        return cleanKey
    }
}

@Composable
actual fun rememberResourceResolver(): ResourceResolver {
    return remember { IosResourceResolver() }
}
