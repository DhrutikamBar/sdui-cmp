package com.example.sdui.app

import com.example.sdui.shared.UiNode
import kotlinx.serialization.json.Json

/**
 * Local fallback screens for development and resilience.
 * Updated to use natural JSON format supported by the custom SduiValue serializer.
 */
object LocalScreens {

    val home = """
        {
          "type": "column",
          "props": {
            "style": { "padding": "md" }
          },
          "children": [
            { 
              "type": "text", 
              "sticky": true,
              "onAppear": { "type": "analytics", "target": "sign_up_header_impression" },
              "props": { 
                "value": "Sign up",
                "style": { "fontSize": 22, "fontWeight": "bold", "background": "surface", "width": "fill" }
              }
            },
            {
              "id": "ageField", "type": "textInput",
              "props": { "label": "Your age", "keyboardType": "number" }
            },
            {
              "id": "bulkDiscount",
              "type": "text",
              "visibleWhen": [ { "type": "script", "expression": "ageField > 18" } ],
              "props": { 
                "value": "Adult Content Unlocked",
                "style": { "color": "brand-primary" }
              }
            },
            {
              "id": "priceField", "type": "textInput",
              "props": { "label": "Unit Price", "keyboardType": "number" }
            },
            {
              "id": "qtyField", "type": "textInput",
              "props": { "label": "Quantity", "keyboardType": "number" }
            },
            {
              "type": "text",
              "visibleWhen": [ { "type": "script", "expression": "priceField * qtyField > 100" } ],
              "props": { 
                "value": "Elite Bulk Discount Applied!",
                "style": { "color": "#2E7D32", "fontWeight": "bold" }
              }
            },
            {
              "id": "lottieButton",
              "type": "button",
              "props": { "label": "Lottie Test" },
              "action": { "type": "navigate", "target": "lottie-test" }
            },
            {
              "id": "submitButton",
              "type": "button",
              "props": { "label": "Submit" },
              "action": { 
                "type": "navigate", 
                "target": "welcome",
                "feedback": { "type": "haptic", "intensity": "heavy" }
              },
              "rules": [ { "type": "notEmpty", "field": "ageField" } ]
            }
          ]
        }
    """.trimIndent()

    val welcome = """
        {
          "type": "column",
          "props": { "style": { "padding": "xl", "alignment": "center" } },
          "children": [
            { "type": "text", "props": { "value": "Welcome!", "style": { "fontSize": 32, "fontWeight": "bold" } } },
            { "type": "spacer", "props": { "style": { "size": "md" } } },
            { "type": "text", "props": { "value": "Your account has been created successfully." } },
            { "type": "spacer", "props": { "size": 24 } },
            { "type": "button", "props": { "label": "Go to Wallet" }, "action": { "type": "navigate", "target": "wallet" } }
          ]
        }
    """.trimIndent()

    val wallet = """
        {
          "type": "column",
          "props": { "style": { "padding": "md", "background": "brand-primary", "width": "fill" } },
          "children": [
            {
              "type": "row",
              "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
              "children": [
                { "type": "column", "children": [
                  { "type": "text", "props": { "value": "Welcome back,", "style": { "fontSize": 13, "color": "#B0BEC5" } } },
                  { "type": "text", "props": { "value": "Tanjiro Kamado", "style": { "fontSize": 17, "fontWeight": "bold", "color": "#FFFFFF" } } }
                ]}
              ]
            },
            { "type": "spacer", "props": { "style": { "size": "spacing-xl" } } },
            { "type": "nativeSlot", "props": { "id": "balanceToggle", "amount": "${'$'}32,149.00" } }
          ]
        }
    """.trimIndent()

    val checkout = """
        {
          "type": "column",
          "props": { "style": { "padding": "md", "width": "fill", "animateSize": true } },
          "children": [
            { "type": "text", "props": { "value": "Checkout", "style": { "fontSize": 22, "fontWeight": "bold" } } },
            { "type": "spacer", "props": { "style": { "size": "spacing-md" } } },
            { 
              "type": "image", 
              "props": { 
                "url": "https://picsum.photos/seed/headphones/400/400", 
                "style": { "cornerRadius": 12, "size": 180 } 
              },
              "semantics": { "contentDescription": "Photo of the wireless headphones", "role": "image" }
            },
            { "type": "spacer", "props": { "style": { "size": "spacing-sm" } } },
            { "type": "text", "props": { "value": "Wireless Headphones", "style": { "fontWeight": "bold" } } },
            { "type": "text", "props": { "value": "${'$'}59.99" } },
            { "type": "rating", "props": { "value": 4, "max": 5 } },
            { "type": "spacer", "props": { "style": { "size": "md" } } },
            {
              "id": "agreeTerms", "type": "checkbox", "props": { "label": "I agree to the terms and refund policy" }
            },
            {
              "type": "text",
              "props": { "value": "Thanks for confirming — you're ready to check out.", "style": { "color": "#2E7D32", "fontSize": 13 } },
              "visibleWhen": [ { "type": "isTrue", "field": "agreeTerms" } ]
            },
            { "type": "spacer", "props": { "style": { "size": "md" } } },
            {
              "id": "placeOrderButton",
              "type": "button",
              "props": { "label": "Place order" },
              "action": {
                "type": "apiCall",
                "method": "POST",
                "target": "/api/orders",
                "body": { "productId": "p1", "paymentMethod": "{{paymentMethod}}" },
                "onSuccess": { "type": "navigate", "target": "order-confirmed" },
                "onError": { "type": "toggleState", "target": "orderErrorSnackbar" }
              },
              "rules": [ { "type": "isTrue", "field": "agreeTerms" } ]
            },
            { "id": "orderErrorSnackbar", "type": "snackbar", "props": { "message": "Couldn't place your order — please try again" } }
          ]
        }
    """.trimIndent()
}

private val localJson = Json { ignoreUnknownKeys = true }

fun decodeLocalScreen(json: String): UiNode = localJson.decodeFromString(json)
