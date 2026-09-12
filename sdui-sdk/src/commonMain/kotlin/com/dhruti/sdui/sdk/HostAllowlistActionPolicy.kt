package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction

/**
 * Reusable deny-by-default policy for hosts that expose remote SDUI actions.
 *
 * Exact API paths and HTTPS host names must be explicitly supplied by the host.
 * This policy deliberately knows nothing about authentication or tenant access;
 * those remain host responsibilities.
 */
class HostAllowlistActionPolicy(
    private val localRoutes: Set<String> = emptySet(),
    private val allowedHttpsHosts: Set<String> = emptySet(),
    private val allowedApiEndpoints: Map<String, Set<String>> = emptyMap(),
    private val allowedStateKeys: Set<String> = emptySet(),
    private val allowedAnalyticsEvents: Set<String> = emptySet()
) : SduiActionPolicy {
    override fun evaluate(action: UiAction): SduiActionDecision = when (action.type) {
        "navigate" -> allow(action.target in localRoutes, "Route is not allowlisted")
        "back" -> allow(action.target == null, "Back action must not have a target")
        "openUrl" -> allow(isAllowedHttpsUrl(action.target), "URL host is not allowlisted")
        "apiCall" -> allow(isAllowedApiCall(action), "API endpoint is not allowlisted")
        "toggleState" -> allow(action.target in allowedStateKeys, "State key is not allowlisted")
        "analytics" -> allow(action.target in allowedAnalyticsEvents, "Analytics event is not allowlisted")
        else -> SduiActionDecision.Deny("Action type is not allowlisted: ${action.type}")
    }

    private fun isAllowedHttpsUrl(value: String?): Boolean {
        if (value.isNullOrBlank() || value.length > 2_048 || value.any(Char::isWhitespace)) return false
        val authority = value.removePrefix("https://").takeIf { value.startsWith("https://") }
            ?.substringBefore('/')
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.lowercase()
            ?: return false
        return authority in allowedHttpsHosts
    }

    private fun isAllowedApiCall(action: UiAction): Boolean {
        val method = (action.method ?: "POST").uppercase()
        val endpoint = action.target ?: return false
        return endpoint in (allowedApiEndpoints[method] ?: emptySet())
    }

    private fun allow(condition: Boolean, message: String): SduiActionDecision =
        if (condition) SduiActionDecision.Allow else SduiActionDecision.Deny(message)
}
