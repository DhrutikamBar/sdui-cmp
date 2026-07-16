package com.example.sdui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import com.example.sdui.shared.UiNode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

data class Style(
    val padding: JsonPrimitive? = null,
    val background: String? = null,
    val cornerRadius: Int? = null,
    val shape: String? = null,
    val color: String? = null,
    val fontSize: Int? = null,
    val fontWeight: String? = null,
    val arrangement: String? = null,
    val alignment: String? = null,
    val width: String? = null,
    val size: JsonPrimitive? = null,
    val scrollable: Boolean? = null,
    val animation: String? = null,
    val animateSize: Boolean? = null
)

fun UiNode.style(): Style {
    val obj = props["style"] as? JsonObject ?: return Style()
    return Style(
        padding = obj["padding"]?.jsonPrimitive,
        background = obj["background"]?.jsonPrimitive?.contentOrNull,
        cornerRadius = obj["cornerRadius"]?.jsonPrimitive?.intOrNull,
        shape = obj["shape"]?.jsonPrimitive?.contentOrNull,
        color = obj["color"]?.jsonPrimitive?.contentOrNull,
        fontSize = obj["fontSize"]?.jsonPrimitive?.intOrNull,
        fontWeight = obj["fontWeight"]?.jsonPrimitive?.contentOrNull,
        arrangement = obj["arrangement"]?.jsonPrimitive?.contentOrNull,
        alignment = obj["alignment"]?.jsonPrimitive?.contentOrNull,
        width = obj["width"]?.jsonPrimitive?.contentOrNull,
        size = obj["size"]?.jsonPrimitive,
        scrollable = obj["scrollable"]?.jsonPrimitive?.booleanOrNull,
        animation = obj["animation"]?.jsonPrimitive?.contentOrNull,
        animateSize = obj["animateSize"]?.jsonPrimitive?.booleanOrNull
    )
}

fun resolveSpacing(value: JsonPrimitive?): Dp {
    value ?: return 0.dp
    value.intOrNull?.let { return it.dp }
    return when (value.contentOrNull) {
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
    val scheme = MaterialTheme.colorScheme
    return when (value) {
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
        else -> if (value.startsWith("#")) parseColor(value) else null
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
    Text(
        text = value,
        modifier = modifier,
        color = resolveColor(style.color) ?: Color.Unspecified,
        fontSize = if (style.fontSize != null) style.fontSize.sp else androidx.compose.ui.unit.TextUnit.Unspecified,
        fontWeight = style.fontWeight?.let(::parseFontWeight)
    )
}