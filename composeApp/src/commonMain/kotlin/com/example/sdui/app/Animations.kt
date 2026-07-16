package com.example.sdui.app


import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val DEFAULT_DURATION_MS = 250

fun enterAnimation(kind: String?): EnterTransition = when (kind) {
    "slide" -> slideInVertically(tween(DEFAULT_DURATION_MS)) { it } + fadeIn(tween(DEFAULT_DURATION_MS))
    "scale" -> scaleIn(tween(DEFAULT_DURATION_MS)) + fadeIn(tween(DEFAULT_DURATION_MS))
    else -> fadeIn(tween(DEFAULT_DURATION_MS)) + expandVertically(tween(DEFAULT_DURATION_MS))
}

fun exitAnimation(kind: String?): ExitTransition = when (kind) {
    "slide" -> slideOutVertically(tween(DEFAULT_DURATION_MS)) { it } + fadeOut(tween(DEFAULT_DURATION_MS))
    "scale" -> scaleOut(tween(DEFAULT_DURATION_MS)) + fadeOut(tween(DEFAULT_DURATION_MS))
    else -> fadeOut(tween(DEFAULT_DURATION_MS)) + shrinkVertically(tween(DEFAULT_DURATION_MS))
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