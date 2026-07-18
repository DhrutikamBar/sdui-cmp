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

/**
 * [type] is the only fixed part — a small, closed set of action *verbs* the client knows how
 * to execute ("navigate", "apiCall", "toggleState", "openUrl"). Everything else is data, so a
 * specific business action ("add to cart", "submit review", "toggle favorite") is just an
 * `apiCall` configured differently — never a new action type, never new client code.
 *
 * [target] means different things per type: a route path for "navigate", a FormState key for
 * "toggleState", a URL for "openUrl"/"apiCall".
 * [method] / [body] are for "apiCall" — body values starting with "{{" and ending with "}}"
 * are resolved against FormState by field id before the request is sent.
 * [onSuccess] / [onError] let the backend chain a follow-up action to an "apiCall" outcome —
 * show a snackbar, navigate, toggle a dialog — entirely from JSON.
 */
@Serializable
data class UiAction(
    val type: String,
    val target: String? = null,
    val method: String? = null,
    val body: JsonObject? = null,
    val onSuccess: UiAction? = null,
    val onError: UiAction? = null
)

/**
 * A minimal rule: "this node is only enabled once [whenExpr] is satisfied".
 * whenExpr is "<fieldId>.<check>", e.g. "ageField.notEmpty" — the age-field example
 * from PhonePe's LiquidUI. Extend the `check` branch in Rule.isSatisfied (composeApp)
 * to add more comparisons; don't reach for a full expression parser until you need one.
 */
@Serializable
data class Rule(val whenExpr: String)