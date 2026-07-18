package com.example.sdui.app

import kotlinx.serialization.Serializable

/**
 * Navigation Compose wants routes known at compile time (@Serializable classes). SDUI targets
 * are runtime strings from JSON — the server can send a path nobody wrote a Kotlin class for.
 * The reconciliation: ONE route type, whose payload *is* the dynamic path, rather than one
 * route per screen. Every server-driven screen is a SduiScreen; only genuinely native,
 * non-SDUI destinations (a real camera flow, say) would get their own dedicated route type
 * alongside this one.
 *
 * Deliberately no path -> screen table here, even for local mode. In server mode the path
 * from an action's `target` becomes the literal URL fetched — the client never has a list
 * of "valid" paths or what they mean, the backend owns that entirely. Local mode has no
 * backend to ask, so it doesn't get to pretend to route either: it always shows one
 * designated preview screen (see App.kt), regardless of what path an action requested.
 */
@Serializable
data class SduiScreen(val path: String)