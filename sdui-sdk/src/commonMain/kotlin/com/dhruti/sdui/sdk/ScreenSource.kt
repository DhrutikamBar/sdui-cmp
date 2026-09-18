package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiNode
import kotlin.coroutines.cancellation.CancellationException

/** A request for a server-driven screen. */
data class ScreenRequest(
    val path: String,
    val forceRefresh: Boolean = false
)

/** Where a [ScreenLoadResult.Success] was resolved. */
enum class ScreenLoadSource {
    UNKNOWN,
    MEMORY,
    DISK,
    NETWORK
}

/**
 * Typed result returned by [ScreenSource.loadScreen].
 *
 * Cancellation is never converted to [Failure]; callers retain normal coroutine
 * cancellation semantics.
 */
sealed interface ScreenLoadResult {
    data class Success(
        val screen: UiNode,
        val source: ScreenLoadSource = ScreenLoadSource.UNKNOWN
    ) : ScreenLoadResult

    data class Failure(val cause: Throwable) : ScreenLoadResult
}

/**
 * Host-owned screen delivery contract.
 *
 * Existing sources can implement [fetchScreen] and inherit [loadScreen]. Sources
 * with cache metadata may override [loadScreen] to report a precise [ScreenLoadSource].
 */
interface ScreenSource {
    suspend fun fetchScreen(path: String, forceRefresh: Boolean = false): UiNode

    suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult = try {
        ScreenLoadResult.Success(fetchScreen(request.path, request.forceRefresh))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (cause: Throwable) {
        ScreenLoadResult.Failure(cause)
    }

    fun prefetch(path: String)

    /** Cancels a best-effort prefetch when the host no longer needs it. */
    fun cancelPrefetch(path: String) = Unit

    /**
     * Releases source-owned work. Hosts should call this when the source leaves
     * composition or its owning lifecycle ends.
     */
    fun close() = Unit
}
