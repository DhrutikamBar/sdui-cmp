package com.dhruti.sdui.sdk

import androidx.compose.runtime.compositionLocalOf

enum class SduiRemoteResourceType { IMAGE, LOTTIE }

/** Host-controlled policy for server-provided remote media URLs. */
fun interface SduiResourcePolicy {
    fun allows(type: SduiRemoteResourceType, url: String): Boolean
}

/** Compatibility default. Production hosts should provide a strict policy. */
val AllowAllSduiResourcePolicy = SduiResourcePolicy { _, _ -> true }

/**
 * HTTPS-only policy that accepts exact hosts supplied by the host application.
 * It deliberately does not support wildcard or substring matching.
 */
class HostAllowlistResourcePolicy(
    private val allowedHosts: Set<String>
) : SduiResourcePolicy {
    override fun allows(type: SduiRemoteResourceType, url: String): Boolean {
        if (!url.startsWith("https://") || url.length > 2_048 || url.any(Char::isWhitespace)) return false
        val host = url.removePrefix("https://")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .lowercase()
        return host in allowedHosts
    }
}

val LocalResourcePolicy = compositionLocalOf<SduiResourcePolicy> { AllowAllSduiResourcePolicy }
