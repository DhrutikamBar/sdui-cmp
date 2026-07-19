package com.example.sdui.app

import androidx.compose.runtime.Composable

/**
 * Resolves local resources (strings, images) from server-sent identifiers.
 */
interface ResourceResolver {
    fun resolveString(key: String): String
    @Composable fun resolveImage(key: String): Any? // returns Painter or ImageVector or URL
}

@Composable
expect fun rememberResourceResolver(): ResourceResolver

fun String.isResource(): Boolean = startsWith("res://") || startsWith("string-res://")
