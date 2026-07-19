package com.example.sdui.server

import com.example.sdui.shared.Condition
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiAction
import com.example.sdui.shared.UiNode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

private data class Product(val id: String, val name: String, val price: Double)

private val fakeProductApi = listOf(
    Product("p1", "Wireless Headphones", 59.99),
    Product("p2", "Mechanical Keyboard", 89.00),
)

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) { 
            json() 
            protobuf(ProtoBuf)
        }
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
        props = mapOf(
            "value" to SduiValue.StringValue("Sign up"),
            "style" to SduiValue.ObjectValue(mapOf(
                "fontSize" to SduiValue.NumberValue(22.0),
                "fontWeight" to SduiValue.StringValue("bold")
            ))
        )
    )

    val ageField = UiNode(
        id = "ageField",
        type = "textInput",
        props = mapOf(
            "label" to SduiValue.StringValue("Your age")
        )
    )

    val submitButton = UiNode(
        id = "submitButton",
        type = "button",
        props = mapOf("label" to SduiValue.StringValue("Submit")),
        action = UiAction(type = "navigate", target = "/welcome"),
        rules = listOf(Condition.NotEmpty("ageField"))
    )

    val productCards = fakeProductApi.map { product ->
        UiNode(
            type = "box",
            props = mapOf(
                "style" to SduiValue.ObjectValue(mapOf(
                    "padding" to SduiValue.NumberValue(12.0),
                    "background" to SduiValue.StringValue("surface"),
                    "cornerRadius" to SduiValue.NumberValue(12.0),
                    "width" to SduiValue.StringValue("fill")
                ))
            ),
            action = UiAction(type = "navigate", target = "/product/${product.id}"),
            children = listOf(
                UiNode(
                    type = "column",
                    children = listOf(
                        UiNode(
                            type = "text",
                            props = mapOf(
                                "value" to SduiValue.StringValue(product.name),
                                "style" to SduiValue.ObjectValue(mapOf("fontWeight" to SduiValue.StringValue("bold")))
                            )
                        ),
                        UiNode(
                            type = "text",
                            props = mapOf("value" to SduiValue.StringValue("$${product.price}"))
                        )
                    )
                )
            )
        )
    }

    return UiNode(
        type = "column",
        props = mapOf("style" to SduiValue.ObjectValue(mapOf("padding" to SduiValue.StringValue("md")))),
        children = listOf(header, ageField, submitButton) + productCards
    )
}
