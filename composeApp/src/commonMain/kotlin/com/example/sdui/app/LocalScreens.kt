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
              "id": "priceField", "type": "textInput",
              "props": { "label": { "type": "string", "value": "Unit Price" }, "keyboardType": { "type": "string", "value": "number" } }
            },
            {
              "id": "qtyField", "type": "textInput",
              "props": { "label": { "type": "string", "value": "Quantity" }, "keyboardType": { "type": "string", "value": "number" } }
            },
            {
              "type": "text",
              "visibleWhen": [ { "type": "script", "expression": "priceField * qtyField > 100" } ],
              "props": { 
                "value": { "type": "string", "value": "Elite Bulk Discount Applied!" },
                "style": { "type": "object", "value": { "color": { "type": "string", "value": "#2E7D32" }, "fontWeight": { "type": "string", "value": "bold" } } }
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

    val settings = """
    {
      "type": "column",
      "props": {
        "style": { "type": "object", "value": { "padding": { "type": "string", "value": "md" }, "width": { "type": "string", "value": "fill" }, "scrollable": { "type": "boolean", "value": true } } }
      },
      "children": [
        {
          "type": "row",
          "props": { "style": { "type": "object", "value": { "arrangement": { "type": "string", "value": "spaceBetween" }, "width": { "type": "string", "value": "fill" } } } },
          "children": [
            { "type": "text", "props": { "value": { "type": "string", "value": "Settings" }, "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 22 }, "fontWeight": { "type": "string", "value": "bold" } } } } },
            { "type": "icon", "props": { "name": { "type": "string", "value": "settings" } } }
          ]
        },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "md" } } } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Notifications" }, "style": { "type": "object", "value": { "fontWeight": { "type": "string", "value": "bold" } } } } },
        { "id": "pushNotif", "type": "switch", "props": { "label": { "type": "string", "value": "Push notifications" } } },
        { "id": "emailNotif", "type": "switch", "props": { "label": { "type": "string", "value": "Email notifications" } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Notification volume" }, "style": { "type": "object", "value": { "fontSize": { "type": "number", "value": 13 }, "color": { "type": "string", "value": "onSurfaceVariant" } } } } },
        { "id": "notifVolume", "type": "slider", "props": { "min": { "type": "number", "value": 0 }, "max": { "type": "number", "value": 100 }, "default": { "type": "number", "value": 70 } } },
        { "type": "divider" },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "sm" } } } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Subscription plan" }, "style": { "type": "object", "value": { "fontWeight": { "type": "string", "value": "bold" } } } } },
        {
          "id": "plan", "type": "radioGroup",
          "props": { "options": { "type": "list", "value": [ { "type": "string", "value": "Basic" }, { "type": "string", "value": "Pro" }, { "type": "string", "value": "Team" } ] } }
        },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "sm" } } } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Theme" }, "style": { "type": "object", "value": { "fontWeight": { "type": "string", "value": "bold" } } } } },
        {
          "id": "theme", "type": "dropdown",
          "props": {
            "options": { "type": "list", "value": [ { "type": "string", "value": "System default" }, { "type": "string", "value": "Light" }, { "type": "string", "value": "Dark" } ] },
            "placeholder": { "type": "string", "value": "Choose theme" }
          }
        },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "sm" } } } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Interests" }, "style": { "type": "object", "value": { "fontWeight": { "type": "string", "value": "bold" } } } } },
        {
          "type": "flowRow",
          "children": [
            { "type": "chip", "props": { "label": { "type": "string", "value": "Trading" } } },
            { "type": "chip", "props": { "label": { "type": "string", "value": "Fitness" } } },
            { "type": "chip", "props": { "label": { "type": "string", "value": "Music" } } }
          ]
        },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "sm" } } } } },
        { "type": "text", "props": { "value": { "type": "string", "value": "Storage used: 6.2 GB of 10 GB" } } },
        { "type": "progressBar", "props": { "progress": { "type": "number", "value": 0.62 } } },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "lg" } } } } },
        {
          "id": "saveButton", "type": "button",
          "props": { "label": { "type": "string", "value": "Save changes" } },
          "action": { "type": "toggleState", "target": "savedSnackbar" }
        },
        { "id": "savedSnackbar", "type": "snackbar", "props": { "message": { "type": "string", "value": "Settings saved" } } },
        { "type": "spacer", "props": { "style": { "type": "object", "value": { "size": { "type": "string", "value": "sm" } } } } },
        {
          "id": "logoutButton", "type": "button",
          "props": { "label": { "type": "string", "value": "Log out" } },
          "action": { "type": "toggleState", "target": "logoutDialog" }
        },
        {
          "id": "logoutDialog", "type": "dialog",
          "props": {
            "title": { "type": "string", "value": "Log out?" },
            "confirmLabel": { "type": "string", "value": "Log out" }
          },
          "children": [
            { "type": "text", "props": { "value": { "type": "string", "value": "You'll need to sign in again next time." } } }
          ]
        }
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
            { 
              "type": "image", 
              "props": { 
                "url": { "type": "string", "value": "https://picsum.photos/seed/headphones/400/400" }, 
                "style": { "type": "object", "value": { "cornerRadius": { "type": "number", "value": 12 }, "size": { "type": "number", "value": 180 } } } 
              },
              "semantics": { "contentDescription": "Photo of the wireless headphones", "role": "image" }
            },
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
