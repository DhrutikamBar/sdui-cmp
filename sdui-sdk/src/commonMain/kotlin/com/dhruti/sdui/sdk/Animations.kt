package com.dhruti.sdui.sdk

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val DEFAULT_DURATION_MS = 250

/** Host-controlled accessibility preference for SDUI motion. */
data class SduiMotionPolicy(val reduceMotion: Boolean = false)

val LocalSduiMotionPolicy = compositionLocalOf { SduiMotionPolicy() }

@Composable
fun enterAnimation(kind: String?, durationMs: Int? = null, easingName: String? = null): EnterTransition {
    if (LocalSduiMotionPolicy.current.reduceMotion) return EnterTransition.None
    val duration = durationMs?.coerceIn(0, 2_000) ?: DEFAULT_DURATION_MS
    val easing = animationEasing(easingName)
    val floatSpec = tween<Float>(durationMillis = duration, easing = easing)
    val intSpec = tween<Int>(durationMillis = duration, easing = easing)
    return when (kind) {
        "none" -> EnterTransition.None
        "slide" -> slideInVertically(intSpec) { it } + fadeIn(floatSpec)
        "scale" -> scaleIn(floatSpec) + fadeIn(floatSpec)
        "fade" -> fadeIn(floatSpec)
        else -> fadeIn(floatSpec) + expandVertically(intSpec)
    }
}

@Composable
fun exitAnimation(kind: String?, durationMs: Int? = null, easingName: String? = null): ExitTransition {
    if (LocalSduiMotionPolicy.current.reduceMotion) return ExitTransition.None
    val duration = durationMs?.coerceIn(0, 2_000) ?: DEFAULT_DURATION_MS
    val easing = animationEasing(easingName)
    val floatSpec = tween<Float>(durationMillis = duration, easing = easing)
    val intSpec = tween<Int>(durationMillis = duration, easing = easing)
    return when (kind) {
        "none" -> ExitTransition.None
        "slide" -> slideOutVertically(intSpec) { it } + fadeOut(floatSpec)
        "scale" -> scaleOut(floatSpec) + fadeOut(floatSpec)
        "fade" -> fadeOut(floatSpec)
        else -> fadeOut(floatSpec) + shrinkVertically(intSpec)
    }
}

private fun animationEasing(name: String?) = when (name) {
    "linear" -> LinearEasing
    else -> FastOutSlowInEasing
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, cornerRadius: Int = 4) {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    Box(modifier.clip(RoundedCornerShape(cornerRadius.dp)).background(Color.Gray.copy(alpha = alpha)))
}
