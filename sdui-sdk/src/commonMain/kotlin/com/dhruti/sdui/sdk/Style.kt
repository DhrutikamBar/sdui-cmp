package com.dhruti.sdui.sdk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode

data class DesignTokens(
    val colors: Map<String, String> = mapOf(
        "brand-primary" to "#0D1B4C",
        "brand-secondary" to "#3949AB"
    ),
    val spacing: Map<String, Int> = mapOf(
        "spacing-xs" to 4,
        "spacing-sm" to 8,
        "spacing-md" to 16,
        "spacing-lg" to 24,
        "spacing-xl" to 32
    )
)

val LocalDesignTokens = compositionLocalOf { DesignTokens() }

data class Style(
    val padding: SduiValue? = null,
    val background: String? = null,
    val cornerRadius: Int? = null,
    val shape: String? = null,
    val color: String? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val arrangement: String? = null,
    val alignment: String? = null,
    val width: String? = null,
    val size: SduiValue? = null,
    val scrollable: Boolean? = null,
    val animation: String? = null,
    val animateSize: Boolean? = null
)

fun UiNode.style(): Style {
    val obj = (props["style"] as? SduiValue.ObjectValue)?.value ?: return Style()
    fun SduiValue?.asString() = (this as? SduiValue.StringValue)?.value
    fun SduiValue?.asInt() = (this as? SduiValue.NumberValue)?.value?.toInt()
    fun SduiValue?.asBoolean() = (this as? SduiValue.BooleanValue)?.value

    return Style(
        padding = obj["padding"],
        background = obj["background"].asString(),
        cornerRadius = obj["cornerRadius"].asInt(),
        shape = obj["shape"].asString(),
        color = obj["color"].asString(),
        fontSize = obj["fontSize"].asInt(),
        fontWeight = obj["fontWeight"].asString(),
        arrangement = obj["arrangement"].asString(),
        alignment = obj["alignment"].asString(),
        width = obj["width"].asString(),
        size = obj["size"],
        scrollable = obj["scrollable"].asBoolean(),
        animation = obj["animation"].asString(),
        animateSize = obj["animateSize"].asBoolean()
    )
}

@Composable
fun resolveSpacing(value: SduiValue?): Dp {
    value ?: return 0.dp
    if (value is SduiValue.NumberValue) return value.value.toInt().dp
    val str = (value as? SduiValue.StringValue)?.value ?: return 0.dp
    val tokens = LocalDesignTokens.current
    tokens.spacing[str]?.let { return it.dp }
    return when (str) {
        "xs" -> 4.dp
        "sm" -> 8.dp
        "md" -> 16.dp
        "lg" -> 24.dp
        "xl" -> 32.dp
        else -> 0.dp
    }
}

fun parseColor(hex: String): Color = try {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16)
    Color(if (clean.length == 6) 0xFF000000 or value else value)
} catch (e: NumberFormatException) {
    Color.Black
}

@Composable
fun resolveColor(value: String?): Color? {
    value ?: return null
    val tokens = LocalDesignTokens.current
    val hex = tokens.colors[value] ?: value
    
    val scheme = MaterialTheme.colorScheme
    return when (hex) {
        "primary" -> scheme.primary
        "onPrimary" -> scheme.onPrimary
        "secondary" -> scheme.secondary
        "onSecondary" -> scheme.onSecondary
        "surface" -> scheme.surface
        "onSurface" -> scheme.onSurface
        "surfaceVariant" -> scheme.surfaceVariant
        "onSurfaceVariant" -> scheme.onSurfaceVariant
        "background" -> scheme.background
        "onBackground" -> scheme.onBackground
        "error" -> scheme.error
        "onError" -> scheme.onError
        "outline" -> scheme.outline
        else -> if (hex.startsWith("#")) parseColor(hex) else null
    }
}

fun parseArrangement(s: String?): Arrangement.Horizontal = when (s) {
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceEvenly" -> Arrangement.SpaceEvenly
    "spaceAround" -> Arrangement.SpaceAround
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    else -> Arrangement.Start
}

fun parseColumnAlignment(s: String?): Alignment.Horizontal = when (s) {
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

fun parseBoxAlignment(s: String?): Alignment = if (s == "center") Alignment.Center else Alignment.TopStart

fun parseFontWeight(s: String?): FontWeight = when (s) {
    "bold" -> FontWeight.Bold
    "semibold" -> FontWeight.SemiBold
    "medium" -> FontWeight.Medium
    "light" -> FontWeight.Light
    else -> FontWeight.Normal
}

@Composable
fun Modifier.applyStyle(style: Style): Modifier {
    var m = this
    if (style.width == "fill") m = m.fillMaxWidth()
    style.size?.let { m = m.size(resolveSpacing(it)) }
    style.padding?.let { m = m.padding(resolveSpacing(it)) }
    val shape = when {
        style.shape == "circle" -> CircleShape
        style.cornerRadius != null -> RoundedCornerShape(style.cornerRadius.dp)
        else -> null
    }
    if (shape != null) m = m.clip(shape)
    resolveColor(style.background)?.let { m = m.background(it) }
    return m
}

@Composable
fun StyledText(value: String, style: Style, modifier: Modifier = Modifier) {
    val resolver = LocalResourceResolver.current
    val finalValue = if (value.isResource()) resolver?.resolveString(value) ?: value else value
    Text(
        text = finalValue,
        modifier = modifier,
        color = resolveColor(style.color) ?: Color.Unspecified,
        fontSize = if (style.fontSize != null) style.fontSize.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
        fontWeight = style.fontWeight?.let(::parseFontWeight)
    )
}
