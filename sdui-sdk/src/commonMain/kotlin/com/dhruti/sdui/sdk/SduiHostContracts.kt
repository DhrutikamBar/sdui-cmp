package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction

/**
 * Host-owned navigation capability exposed to server-driven actions.
 *
 * The SDK does not own a navigation library. Applications implement this contract
 * and decide how a server path maps to their navigation stack.
 */
interface SduiNavigator {
    fun navigate(path: String)
    fun goBack()
}

/**
 * Host-owned capability for opening external URLs.
 *
 * Implementations must validate schemes and destinations appropriate for the host
 * application before opening a URL.
 */
fun interface SduiUrlHandler {
    fun open(url: String)
}

/**
 * Evaluates whether the host permits a server-defined action to run.
 *
 * This contract is intentionally separate from [ActionRegistry]. It introduces no
 * behaviour by itself; hosts can adopt it while existing action dispatch remains
 * unchanged. A later integration phase will apply this decision before dispatch.
 */
fun interface SduiActionPolicy {
    fun evaluate(action: UiAction): SduiActionDecision
}

/** A host decision for a server-defined [UiAction]. */
sealed interface SduiActionDecision {
    data object Allow : SduiActionDecision
    data class Deny(val reason: String) : SduiActionDecision
}

/**
 * Compatibility policy for hosts that have not yet adopted action restrictions.
 *
 * Use only as an explicit transitional choice; production hosts should provide an
 * allowlisting [SduiActionPolicy].
 */
object AllowAllSduiActionPolicy : SduiActionPolicy {
    override fun evaluate(action: UiAction): SduiActionDecision = SduiActionDecision.Allow
}
