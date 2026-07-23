package com.dhruti.sdui.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

class AndroidResourceResolver(private val context: android.content.Context) : ResourceResolver {
    override fun resolveString(key: String): String {
        val cleanKey = key.removePrefix("string-res://")
        val id = context.resources.getIdentifier(cleanKey, "string", context.packageName)
        return if (id != 0) context.getString(id) else key
    }

    @Composable
    override fun resolveImage(key: String): Any? {
        val cleanKey = key.removePrefix("res://")
        val id = context.resources.getIdentifier(cleanKey, "drawable", context.packageName)
        return if (id != 0) id else key
    }
}

@Composable
actual fun rememberResourceResolver(): ResourceResolver {
    val context = LocalContext.current
    return androidx.compose.runtime.remember(context) { AndroidResourceResolver(context) }
}
