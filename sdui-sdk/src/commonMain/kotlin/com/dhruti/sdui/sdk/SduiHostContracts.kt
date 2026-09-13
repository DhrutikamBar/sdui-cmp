package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
 * behaviour by itself. The reference host passes this policy to [ActionRegistry];
 * other hosts must do the same to enforce their decisions before dispatch.
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


/**
 * Host-owned executor for an allowed server-defined `apiCall` action.
 *
 * The SDK defines no HTTP stack, base URL, credentials, or retry policy. Hosts
 * validate and execute requests with their own transport, returning `true` only
 * when the action should dispatch its `onSuccess` follow-up.
 */
interface SduiApiCallClient {
    suspend fun execute(action: UiAction, formState: FormState): Boolean
}


/**
 * Host-owned, observable data for an SDUI route. The host maps its own API
 * models into [SduiDataContext]; the SDK never sees backend-specific types.
 */
interface SduiScreenDataProvider {
    fun stateFor(path: String): StateFlow<SduiDataState<SduiDataContext>>

    /** Refreshes the values for a route when the host supports refresh. */
    suspend fun refresh(path: String) = Unit
}

/** Default provider preserves static SDUI behaviour for hosts without live data. */
object EmptySduiScreenDataProvider : SduiScreenDataProvider {
    private val state = MutableStateFlow<SduiDataState<SduiDataContext>>(
        SduiDataState.Content(SduiDataContext())
    )

    override fun stateFor(path: String): StateFlow<SduiDataState<SduiDataContext>> = state
}
