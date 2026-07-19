package com.example.sdui.app

import com.example.sdui.shared.UiNode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Updated with "Ultimate Elite" features.
 */
object LocalScreens {

    val home = """
        {
          "type": "column",
          "props": {
            "style": { "type": "object", "value": { "padding": { "type": "string", "value": "md" } } }
          },
          "children": [
            { 
              "type": "text", 
              "sticky": true,
              "onAppear": { "type": "analytics", "target": "sign_up_header_impression" },
              "props": { 
                "value": { "type": "string", "value": "Sign up" },
                "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 22 }, "fontWeight": { "type": "string", "value": "bold" }, "background": { "type": "string", "value": "surface" }, "width": { "type": "string", "value": "fill" } } }
              }
            },
            {
              "id": "ageField", "type": "textInput",
              "props": { "label": { "type": "string", "value": "Your age" }, "keyboardType": { "type": "string", "value": "number" } }
            },
            {
              "id": "bulkDiscount",
              "type": "text",
              "visibleWhen": [ { "type": "script", "expression": "ageField > 18" } ],
              "props": { 
                "value": { "type": "string", "value": "Adult Content Unlocked" },
                "style": { "type": "object", "value": { "color": { "type": "string", "value": "brand-primary" } } }
              }
            },
            {
              "id": "submitButton",
              "type": "button",
              "props": { "label": { "type": "string", "value": "Submit" } },
              "action": { 
                "type": "navigate", 
                "target": "/welcome",
                "feedback": { "type": "haptic", "value": { "type": "string", "value": "heavy" } }
              },
              "rules": [ { "type": "notEmpty", "field": "ageField" } ]
            }
          ]
        }
    """.trimIndent()

    val wallet = """
        {
          "type": "column",
          "props": { "style": { "type": "object", "value": { "padding": { "type": "string", "value": "md" }, "background": { "type": "string", "value": "brand-primary" }, "width": { "type": "string", "value": "fill" } } } },
          "children": [
            {
              "type": "row",
              "props": { "style": { "type": "object", "value": { "arrangement": { "type": "string", "value": "spaceBetween" }, "width": { "type": "string", "value": "fill" } } } },
              "children": [
                { "type": "column", "children": [
                  { "type": "text", "props": { "value": { "type": "string", "value": "Welcome back," }, "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 13 }, "color": { "type": "string", "value": "#B0BEC5" } } } } },
                  { "type": "text", "props": { "value": { "type": "string", "value": "Tanjiro Kamado" }, "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 17 }, "fontWeight": { "type": "string", "value": "bold" }, "color": { "type": "string", "value": "#FFFFFF" } } } } }
                ]}
              ]
            },
            { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "spacing-xl" } } } } },
            { "type": "nativeSlot", "props": { "id": { "type": "string", "value": "balanceToggle" }, "amount": { "type": "string", "value": "${'$'}32,149.00" } } }
          ]
        }
    """.trimIndent()

    val checkout = """
        {
          "type": "column",
          "props": { "style": { "type": "object", "value": { "padding": { "type": "string", "value": "md" }, "width": { "type": "string", "value": "fill" }, "animateSize": { "type": "boolean", "value": true } } } },
          "children": [
            { "type": "text", "props": { "value": { "type": "string", "value": "Checkout" }, "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 22 }, "fontWeight": { "type": "string", "value": "bold" } } } } },
            { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "spacing-md" } } } } },
            { "type": "image", "props": { "url": { "type": "string", "value": "https://picsum.photos/seed/headphones/400/400" }, "style": { "type": "object", "value": { "cornerRadius": { "type": "number", "value": 12 }, "size": { "type": "number", "value": 180 } } } } },
            { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "spacing-sm" } } } } },
            { "type": "text", "props": { "value": { "type": "string", "value": "Wireless Headphones" }, "style": { "type": "object", "value": { "fontWeight": { "type": "string", "value": "bold" } } } } },
            { "type": "text", "props": { "value": { "type": "string", "value": "${'$'}59.99" } } },
            { "type": "rating", "props": { "value": { "type": "number", "value": 4 }, "max": { "type": "number", "value": 5 } } },
            { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "md" } } } } },
            {
              "id": "agreeTerms", "type": "checkbox", "props": { "label": { "type": "string", "value": "I agree to the terms and refund policy" } }
            },
            {
              "type": "text",
              "props": { "value": { "type": "string", "value": "Thanks for confirming — you're ready to check out." }, "style": { "type": "object", "value": { "color": { "type": "string", "value": "#2E7D32" }, "fontSize": { "type": "number", "value": 13 } } } },
              "visibleWhen": [ { "type": "isTrue", "field": "agreeTerms" } ]
            },
            { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "md" } } } } },
            {
              "id": "placeOrderButton",
              "type": "button",
              "props": { "label": { "type": "string", "value": "Place order" } },
              "action": {
                "type": "apiCall",
                "method": "POST",
                "target": "/api/orders",
                "body": { "productId": { "type": "string", "value": "p1" }, "paymentMethod": { "type": "string", "value": "{{paymentMethod}}" } },
                "onSuccess": { "type": "navigate", "target": "/order-confirmed" },
                "onError": { "type": "toggleState", "target": "orderErrorSnackbar" }
              },
              "rules": [ { "type": "isTrue", "field": "agreeTerms" } ]
            },
            { "id": "orderErrorSnackbar", "type": "snackbar", "props": { "message": { "type": "string", "value": "Couldn't place your order — please try again" } } }
          ]
        }
    """.trimIndent()
}

private val localJson = Json { ignoreUnknownKeys = true }

fun decodeLocalScreen(json: String): UiNode = localJson.decodeFromString(json)
