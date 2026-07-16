package com.example.sdui.server

import com.example.sdui.shared.Rule
import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Stand-in for "your API integration" — swap this for a real database/service call.
// The `.map` below, turning domain objects into UiNode.Card, is the entire data-binding pattern.
private data class Product(val id: String, val name: String, val price: Double)

private val fakeProductApi = listOf(
    Product("p1", "Wireless Headphones", 59.99),
    Product("p2", "Mechanical Keyboard", 89.00),
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { json() }
        routing {
            get("/api/ui/home") {
                call.respond(buildHomeScreen())
            }
        }
    }.start(wait = true)
}

private fun buildHomeScreen(): UiNode {
    val header = UiNode(
        type = "text",
        props = buildJsonObject { put("value", JsonPrimitive("Sign up")) }
    )

    val ageField = UiNode(
        id = "ageField",
        type = "textInput",
        props = buildJsonObject { put("label", JsonPrimitive("Your age")) }
    )

    // Rules-engine demo: this button only enables once ageField is non-empty —
    // the exact example PhonePe describes for LiquidUI's Rule mechanism.
    val submitButton = UiNode(
        id = "submitButton",
        type = "button",
        props = buildJsonObject { put("label", JsonPrimitive("Submit")) },
        action = UiAction(type = "navigate", target = "/welcome"),
        rules = listOf(Rule(whenExpr = "ageField.notEmpty"))
    )

    // Data-binding demo: real ("your API") data resolved into UiNode.Card before it
    // ever reaches the client, exactly as discussed a few messages back.
    val productCards = fakeProductApi.map { product ->
        UiNode(
            type = "box",
            props = buildJsonObject {
                put("style", buildJsonObject {
                    put("padding", JsonPrimitive(12))
                    put("background", JsonPrimitive("#FFFFFF"))
                    put("cornerRadius", JsonPrimitive(12))
                    put("width", JsonPrimitive("fill"))
                })
            },
            action = UiAction(type = "navigate", target = "/product/${product.id}"),
            children = listOf(
                UiNode(
                    type = "column",
                    children = listOf(
                        UiNode(
                            type = "text",
                            props = buildJsonObject {
                                put("value", JsonPrimitive(product.name))
                                put("style", buildJsonObject { put("fontWeight", JsonPrimitive("bold")) })
                            }
                        ),
                        UiNode(
                            type = "text",
                            props = buildJsonObject { put("value", JsonPrimitive("$${product.price}")) }
                        )
                    )
                )
            )
        )
    }

    return UiNode(
        type = "column",
        children = listOf(header, ageField, submitButton) + productCards
    )
}
