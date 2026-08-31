package com.dhruti.sdui.sdk

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.sdui.shared.Feedback
import com.example.sdui.shared.UiAction

/**
 * Example interceptor for analytics.
 */
class AnalyticsInterceptor(private val reporter: ReportingService) : ActionInterceptor {
    override fun intercept(action: UiAction, next: (UiAction) -> Unit) {
        val metadata = action.metadata.mapValues { it.value.toString() }.toMutableMap()
        metadata["action_type"] = action.type
        action.target?.let { metadata["target"] = it }

        reporter.reportEvent("action_fired", metadata)
        next(action)
    }
}

/**
 * Handles haptic and sound feedback for actions.
 */
class FeedbackInterceptor(private val haptics: androidx.compose.ui.hapticfeedback.HapticFeedback) : ActionInterceptor {
    override fun intercept(action: UiAction, next: (UiAction) -> Unit) {
        action.feedback?.let { fb ->
            if (fb is Feedback.Haptic) {
                val type = when (fb.intensity) {
                    "heavy" -> HapticFeedbackType.LongPress
                    "light" -> HapticFeedbackType.TextHandleMove
                    else -> HapticFeedbackType.LongPress
                }
                haptics.performHapticFeedback(type)
            }
        }
        next(action)
    }
}
