package com.example.sdui.app

import com.example.sdui.shared.UiNode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Four screens, all built from the same registry in Widgets.kt — nothing here needed a new
 * registration, only different JSON. Switch which one renders in App.kt.
 */
object LocalScreens {

    // Rules-engine centerpiece: submit button gated on ageField, plus inline validation.
    val home = """
        {
          "type": "column",
          "props": { "style": { "padding": 16 } },
          "children": [
            { "type": "text", "props": { "value": "Sign up", "style": { "fontSize": 22, "fontWeight": "bold" } } },
            {
              "id": "ageField", "type": "textInput",
              "props": { "label": "Your age", "keyboardType": "number", "errorText": "Please enter a valid number" },
              "errorWhen": [ { "whenExpr": "ageField.isNumber" } ]
            },
            {
              "id": "submitButton",
              "type": "button",
              "props": { "label": "Submit" },
              "action": { "type": "navigate", "target": "/welcome" },
              "rules": [ { "whenExpr": "ageField.notEmpty" } ]
            },
            {
              "type": "box",
              "props": { "style": { "padding": 12, "background": "surface", "cornerRadius": 12, "width": "fill" } },
              "action": { "type": "navigate", "target": "/product/p1" },
              "children": [
                { "type": "column", "children": [
                  { "type": "text", "props": { "value": "Wireless Headphones", "style": { "fontWeight": "bold" } } },
                  { "type": "text", "props": { "value": "$59.99" } }
                ]}
              ]
            },
            {
              "type": "box",
              "props": { "style": { "padding": 12, "background": "surface", "cornerRadius": 12, "width": "fill" } },
              "action": { "type": "navigate", "target": "/product/p2" },
              "children": [
                { "type": "column", "children": [
                  { "type": "text", "props": { "value": "Mechanical Keyboard", "style": { "fontWeight": "bold" } } },
                  { "type": "text", "props": { "value": "$89.00" } }
                ]}
              ]
            }
          ]
        }
    """.trimIndent()

    // Bank-app home screen: real icons, badge, native balance toggle, transactions card.
    val wallet = """
        {
          "type": "column",
          "props": { "style": { "padding": 16, "background": "#0D1B4C", "width": "fill", "scrollable": true } },
          "children": [
            {
              "type": "row",
              "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
              "children": [
                { "type": "column", "children": [
                  { "type": "text", "props": { "value": "Welcome back,", "style": { "fontSize": 13, "color": "#B0BEC5" } } },
                  { "type": "text", "props": { "value": "Tanjiro Kamado", "style": { "fontSize": 17, "fontWeight": "bold", "color": "#FFFFFF" } } }
                ]},
                { "type": "row", "children": [
                  { "type": "icon", "props": { "name": "notifications", "contentDescription": "Notifications", "style": { "color": "#FFFFFF" } } },
                  { "type": "badge", "props": { "count": "3" } },
                  { "type": "spacer", "props": { "style": { "size": 12 } } },
                  { "type": "box", "props": { "style": { "size": 36, "shape": "circle", "background": "#3949AB" } }, "children": [
                    { "type": "text", "props": { "value": "TK", "style": { "color": "#FFFFFF" } } }
                  ]}
                ]}
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 24 } } },
            { "type": "nativeSlot", "props": { "id": "balanceToggle", "amount": "$32,149.00" } },
            { "type": "text", "props": { "value": "Account Balance", "style": { "fontSize": 13, "color": "#B0BEC5" } } },
            { "type": "spacer", "props": { "style": { "size": 20 } } },
            {
              "type": "row",
              "props": { "style": { "arrangement": "spaceEvenly", "width": "fill" } },
              "children": [
                { "type": "column", "action": { "type": "navigate", "target": "/send" }, "props": { "style": { "alignment": "center" } }, "children": [
                  { "type": "box", "props": { "style": { "size": 48, "shape": "circle", "background": "#26339E" } }, "children": [
                    { "type": "icon", "props": { "name": "arrowUp", "style": { "color": "#FFFFFF" } } }
                  ]},
                  { "type": "text", "props": { "value": "Send", "style": { "fontSize": 12, "color": "#FFFFFF" } } }
                ]},
                { "type": "column", "action": { "type": "navigate", "target": "/withdraw" }, "props": { "style": { "alignment": "center" } }, "children": [
                  { "type": "box", "props": { "style": { "size": 48, "shape": "circle", "background": "#26339E" } }, "children": [
                    { "type": "icon", "props": { "name": "arrowDown", "style": { "color": "#FFFFFF" } } }
                  ]},
                  { "type": "text", "props": { "value": "Withdraw", "style": { "fontSize": 12, "color": "#FFFFFF" } } }
                ]},
                { "type": "column", "action": { "type": "navigate", "target": "/invest" }, "props": { "style": { "alignment": "center" } }, "children": [
                  { "type": "box", "props": { "style": { "size": 48, "shape": "circle", "background": "#26339E" } }, "children": [
                    { "type": "icon", "props": { "name": "money", "style": { "color": "#FFFFFF" } } }
                  ]},
                  { "type": "text", "props": { "value": "Invest", "style": { "fontSize": 12, "color": "#FFFFFF" } } }
                ]},
                { "type": "column", "action": { "type": "navigate", "target": "/add" }, "props": { "style": { "alignment": "center" } }, "children": [
                  { "type": "box", "props": { "style": { "size": 48, "shape": "circle", "background": "#26339E" } }, "children": [
                    { "type": "icon", "props": { "name": "add", "style": { "color": "#FFFFFF" } } }
                  ]},
                  { "type": "text", "props": { "value": "Add", "style": { "fontSize": 12, "color": "#FFFFFF" } } }
                ]}
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 20 } } },
            {
              "type": "box",
              "props": { "style": { "padding": 16, "background": "surface", "cornerRadius": 16, "width": "fill" } },
              "children": [
                { "type": "column", "props": { "style": { "width": "fill" } }, "children": [
                  {
                    "type": "row",
                    "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
                    "children": [
                      { "type": "text", "props": { "value": "Transactions", "style": { "fontWeight": "bold" } } },
                      { "type": "text", "action": { "type": "navigate", "target": "/transactions" }, "props": { "value": "See all", "style": { "color": "primary" } } }
                    ]
                  },
                  { "type": "divider" },
                  {
                    "type": "row",
                    "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
                    "children": [
                      { "type": "column", "children": [
                        { "type": "text", "props": { "value": "Subscription payments" } },
                        { "type": "text", "props": { "value": "20 May, 13:28", "style": { "fontSize": 12, "color": "onSurfaceVariant" } } }
                      ]},
                      { "type": "text", "props": { "value": "-$20.00", "style": { "color": "#C62828" } } }
                    ]
                  },
                  {
                    "type": "row",
                    "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
                    "children": [
                      { "type": "column", "children": [
                        { "type": "text", "props": { "value": "Creator payments" } },
                        { "type": "text", "props": { "value": "20 May, 10:32", "style": { "fontSize": 12, "color": "onSurfaceVariant" } } }
                      ]},
                      { "type": "text", "props": { "value": "+$12.99", "style": { "color": "#2E7D32" } } }
                    ]
                  },
                  {
                    "type": "row",
                    "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
                    "children": [
                      { "type": "column", "children": [
                        { "type": "text", "props": { "value": "Purchase payments" } },
                        { "type": "text", "props": { "value": "20 May, 09:24", "style": { "fontSize": 12, "color": "onSurfaceVariant" } } }
                      ]},
                      { "type": "text", "props": { "value": "-$32.00", "style": { "color": "#C62828" } } }
                    ]
                  }
                ]}
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            {
              "type": "box",
              "props": { "style": { "padding": 16, "background": "surface", "cornerRadius": 16, "width": "fill" } },
              "children": [
                {
                  "type": "row",
                  "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
                  "children": [
                    { "type": "column", "children": [
                      { "type": "text", "props": { "value": "Weekly spending: $320", "style": { "fontWeight": "bold" } } },
                      { "type": "text", "props": { "value": "You're staying right on track with your weekly budget", "style": { "fontSize": 12, "color": "onSurfaceVariant" } } }
                    ]},
                    { "type": "icon", "props": { "name": "money" } }
                  ]
                }
              ]
            }
          ]
        }
    """.trimIndent()

    // Preferences centerpiece: switch, slider, radioGroup, dropdown, flowRow of chips,
    // progressBar, plus a dialog and a snackbar triggered from two different buttons.
    val settings = """
        {
          "type": "column",
          "props": { "style": { "padding": 16, "width": "fill", "scrollable": true } },
          "children": [
            {
              "type": "row",
              "props": { "style": { "arrangement": "spaceBetween", "width": "fill" } },
              "children": [
                { "type": "text", "props": { "value": "Settings", "style": { "fontSize": 22, "fontWeight": "bold" } } },
                { "type": "icon", "props": { "name": "settings" } }
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 16 } } },
            { "type": "text", "props": { "value": "Notifications", "style": { "fontWeight": "bold" } } },
            { "id": "pushNotif", "type": "switch", "props": { "label": "Push notifications" } },
            { "id": "emailNotif", "type": "switch", "props": { "label": "Email notifications" } },
            { "type": "text", "props": { "value": "Notification volume", "style": { "fontSize": 13, "color": "onSurfaceVariant" } } },
            { "id": "notifVolume", "type": "slider", "props": { "min": 0, "max": 100, "default": 70 } },
            { "type": "divider" },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "type": "text", "props": { "value": "Subscription plan", "style": { "fontWeight": "bold" } } },
            { "id": "plan", "type": "radioGroup", "props": { "options": [ "Basic", "Pro", "Team" ] } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "type": "text", "props": { "value": "Theme", "style": { "fontWeight": "bold" } } },
            { "id": "theme", "type": "dropdown", "props": { "options": [ "System default", "Light", "Dark" ], "placeholder": "Choose theme" } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "type": "text", "props": { "value": "Interests", "style": { "fontWeight": "bold" } } },
            {
              "type": "flowRow",
              "children": [
                { "type": "chip", "props": { "label": "Trading" } },
                { "type": "chip", "props": { "label": "Fitness" } },
                { "type": "chip", "props": { "label": "Music" } }
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "type": "text", "props": { "value": "Storage used: 6.2 GB of 10 GB" } },
            { "type": "progressBar", "props": { "progress": 0.62 } },
            { "type": "spacer", "props": { "style": { "size": 20 } } },
            {
              "id": "saveButton", "type": "button", "props": { "label": "Save changes" },
              "action": { "type": "toggleState", "target": "savedSnackbar" }
            },
            { "id": "savedSnackbar", "type": "snackbar", "props": { "message": "Settings saved" } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            {
              "id": "logoutButton", "type": "button", "props": { "label": "Log out" },
              "action": { "type": "toggleState", "target": "logoutDialog" }
            },
            {
              "id": "logoutDialog", "type": "dialog",
              "props": { "title": "Log out?", "confirmLabel": "Log out" },
              "children": [ { "type": "text", "props": { "value": "You'll need to sign in again next time." } } ]
            }
          ]
        }
    """.trimIndent()

    // E-commerce flow: network image, rating, tabs, expandable, bottomSheet, checkbox-gated
    // rule with "isTrue", an openUrl link, and the apiCall generalization on Place order —
    // one action that defines endpoint + body + success + failure, entirely from JSON.
    val checkout = """
        {
          "type": "column",
          "props": { "style": { "padding": 16, "width": "fill", "scrollable": true, "animateSize": true } },
          "children": [
            { "type": "text", "props": { "value": "Checkout", "style": { "fontSize": 22, "fontWeight": "bold" } } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "type": "image", "props": { "url": "https://picsum.photos/seed/headphones/400/400", "contentDescription": "Photo of the wireless headphones", "style": { "cornerRadius": 12, "size": 180 } } },
            { "type": "spacer", "props": { "style": { "size": 8 } } },
            { "type": "text", "props": { "value": "Wireless Headphones", "style": { "fontWeight": "bold" } } },
            { "type": "text", "props": { "value": "$59.99" } },
            { "type": "rating", "props": { "value": 4, "max": 5 } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            {
              "type": "flowRow",
              "children": [
                { "type": "chip", "props": { "label": "Free shipping" } },
                { "type": "chip", "props": { "label": "20% off" } }
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            {
              "id": "summaryButton", "type": "button", "props": { "label": "View order summary" },
              "action": { "type": "toggleState", "target": "orderSheet" }
            },
            { "type": "spacer", "props": { "style": { "size": 16 } } },
            {
              "id": "detailTabs", "type": "tabs",
              "props": { "labels": [ "Overview", "Shipping", "Reviews" ] },
              "children": [
                { "type": "text", "props": { "value": "Over-ear wireless headphones with active noise cancellation and 30-hour battery life." } },
                { "type": "text", "props": { "value": "Free shipping, arrives in 3-5 business days." } },
                { "type": "text", "props": { "value": "4.2 average from 1,204 reviews." } }
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            {
              "type": "expandable", "props": { "title": "Return policy" },
              "children": [
                { "type": "text", "props": { "value": "Returns accepted within 30 days in original packaging. Refunds process within 5-7 business days." } }
              ]
            },
            { "type": "spacer", "props": { "style": { "size": 16 } } },
            { "type": "text", "props": { "value": "Payment method", "style": { "fontWeight": "bold" } } },
            { "id": "paymentMethod", "type": "radioGroup", "props": { "options": [ "Card", "UPI", "Cash on delivery" ] } },
            { "type": "spacer", "props": { "style": { "size": 12 } } },
            { "id": "agreeTerms", "type": "checkbox", "props": { "label": "I agree to the terms and refund policy" } },
            {
              "type": "text",
              "props": { "value": "Thanks for confirming — you're ready to check out.", "style": { "color": "#2E7D32", "fontSize": 13 } },
              "visibleWhen": [ { "whenExpr": "agreeTerms.isTrue" } ]
            },
            {
              "type": "text",
              "action": { "type": "openUrl", "target": "https://example.com/terms" },
              "props": { "value": "Read the full terms", "style": { "color": "primary", "fontSize": 13 } }
            },
            { "type": "spacer", "props": { "style": { "size": 16 } } },
            {
              "id": "placeOrderButton",
              "type": "button",
              "props": { "label": "Place order" },
              "action": {
                "type": "apiCall",
                "method": "POST",
                "target": "/api/orders",
                "body": { "productId": "p1", "paymentMethod": "{{paymentMethod}}" },
                "onSuccess": { "type": "navigate", "target": "/order-confirmed" },
                "onError": { "type": "toggleState", "target": "orderErrorSnackbar" }
              },
              "rules": [ { "whenExpr": "agreeTerms.isTrue" } ]
            },
            { "id": "orderErrorSnackbar", "type": "snackbar", "props": { "message": "Couldn't place your order — please try again" } },
            {
              "id": "orderSheet", "type": "bottomSheet",
              "children": [
                { "type": "text", "props": { "value": "Order summary", "style": { "fontWeight": "bold", "fontSize": 18 } } },
                { "type": "divider" },
                { "type": "text", "props": { "value": "Wireless Headphones — $59.99" } },
                { "type": "text", "props": { "value": "Shipping — Free" } },
                { "type": "text", "props": { "value": "Total — $59.99", "style": { "fontWeight": "bold" } } }
              ]
            }
          ]
        }
    """.trimIndent()
}

private val localJson = Json { ignoreUnknownKeys = true }

fun decodeLocalScreen(json: String): UiNode = localJson.decodeFromString(json)