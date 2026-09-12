package com.example.sdui.app

import com.dhruti.sdui.sdk.SduiActionDecision
import com.dhruti.sdui.sdk.SduiActionPolicy
import com.example.sdui.shared.UiAction

/**
 * Baseline allowlist for the reference host.
 *
 * This policy constrains server-provided actions to capabilities implemented by
 * [App]. Production hosts should replace it with a policy that also considers
 * authentication, tenancy, and product-specific authorization.
 */
object DemoSduiActionPolicy : SduiActionPolicy {
    private val allowedApiMethods = setOf("GET", "POST", "PUT", "PATCH", "DELETE")
    private val stateKeyPattern = Regex("^[A-Za-z][A-Za-z0-9_-]{0,63}$")

    override fun evaluate(action: UiAction): SduiActionDecision {
        val target = action.target

        return when (action.type) {
            "navigate" -> allowWhen(
                isLocalRoute(target),
                "Navigation requires a non-empty local route"
            )
            "back" -> allowWhen(
                target == null,
                "Back actions must not define a target"
            )
            "toggleState" -> allowWhen(
                target != null && stateKeyPattern.matches(target),
                "State actions require an identifier-like target"
            )
            "openUrl" -> allowWhen(
                isHttpsUrl(target),
                "Only HTTPS URLs are allowed"
            )
            "apiCall" -> allowWhen(
                isAllowedApiCall(action),
                "API calls require an allowed method and a relative path"
            )
            else -> SduiActionDecision.Deny("Action type is not allowed: ${action.type}")
        }
    }

    private fun allowWhen(condition: Boolean, reason: String): SduiActionDecision =
        if (condition) SduiActionDecision.Allow else SduiActionDecision.Deny(reason)

    private fun isLocalRoute(target: String?): Boolean =
        target != null &&
            target.isNotBlank() &&
            target.length <= 200 &&
            !target.startsWith("/") &&
            !target.contains("://") &&
            target.none(Char::isWhitespace)

    private fun isHttpsUrl(target: String?): Boolean =
        target != null &&
            target.startsWith("https://") &&
            target.length <= 2_048 &&
            target.none(Char::isWhitespace)

    private fun isAllowedApiCall(action: UiAction): Boolean {
        val target = action.target ?: return false
        val method = (action.method ?: "POST").uppercase()

        return method in allowedApiMethods &&
            target.startsWith("/") &&
            !target.startsWith("//") &&
            !target.contains("\\") &&
            target.none(Char::isWhitespace)
    }
}
