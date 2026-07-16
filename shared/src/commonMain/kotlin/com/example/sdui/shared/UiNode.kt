package com.example.sdui.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A single node in the server-driven UI tree.
 *
 * [type] is deliberately just a String, not a sealed-class hierarchy — the SDK's registry
 * (see composeApp) maps it to a renderer at runtime, so a host app can register its own
 * widget types (a candlestick chart, a product carousel, ...) without ever touching this file.
 *
 * [props] is a raw JsonObject rather than named fields for the same reason: every widget
 * type needs different properties, and this module can't know them all in advance.
 */
@Serializable
data class UiNode(
    val id: String? = null,
    val type: String,
    val props: JsonObject = JsonObject(emptyMap()),
    val children: List<UiNode> = emptyList(),
    val action: UiAction? = null,
    val rules: List<Rule> = emptyList(),
    val visibleWhen: List<Rule> = emptyList(),
    val errorWhen: List<Rule> = emptyList()

)

@Serializable
data class UiAction(
    val type: String,
    val target: String? = null
)

/**
 * A minimal rule: "this node is only enabled once [whenExpr] is satisfied".
 * whenExpr is "<fieldId>.<check>", e.g. "ageField.notEmpty" — the age-field example
 * from PhonePe's LiquidUI. Extend the `check` branch in Rule.isSatisfied (composeApp)
 * to add more comparisons; don't reach for a full expression parser until you need one.
 */
@Serializable
data class Rule(val whenExpr: String)
