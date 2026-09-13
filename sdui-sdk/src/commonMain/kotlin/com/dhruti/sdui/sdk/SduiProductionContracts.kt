package com.dhruti.sdui.sdk

/**
 * Stable SDK capability description a host may send with a screen request or
 * use to validate a remotely selected document variant.
 */
data class SduiCapabilities(
    val sdkVersion: Int = SDK_VERSION,
    val widgetTypes: Set<String>,
    val actionTypes: Set<String>
)

const val SDK_VERSION: Int = 5

/**
 * Typed state owned by the host while it supplies data to an SDUI document.
 * The SDK never performs the underlying request.
 */
sealed interface SduiDataState<out T> {
    data object Loading : SduiDataState<Nothing>
    data object Empty : SduiDataState<Nothing>
    data class Content<T>(val value: T, val canLoadMore: Boolean = false) : SduiDataState<T>
    data class Failure(val message: String, val retryable: Boolean = true) : SduiDataState<Nothing>
}

/**
 * Structured result for a host-executed SDUI action.
 * Hosts may use it to drive success/error/retry UI and telemetry.
 */
sealed interface SduiActionResult {
    data object Success : SduiActionResult
    data class Failure(val message: String? = null, val retryable: Boolean = false) : SduiActionResult
    data object Cancelled : SduiActionResult
    data class ValidationFailure(val fieldErrors: Map<String, String> = emptyMap()) : SduiActionResult
}
