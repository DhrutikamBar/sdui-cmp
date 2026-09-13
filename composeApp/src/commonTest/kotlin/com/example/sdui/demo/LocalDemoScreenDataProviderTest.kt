package com.example.sdui.demo

import com.dhruti.sdui.sdk.SduiDataState
import com.dhruti.sdui.sdk.resolveBindings
import com.example.sdui.app.LocalScreens
import com.example.sdui.app.decodeLocalScreen
import com.example.sdui.shared.SduiValue
import com.example.sdui.shared.UiNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LocalDemoScreenDataProviderTest {
    @Test
    fun walletContextResolvesBindingsAndRepeatsTransactions() {
        val provider = LocalDemoScreenDataProvider()
        val state = provider.stateFor("wallet").value
        val content = assertIs<SduiDataState.Content<*>>(state)
        val context = assertIs<com.dhruti.sdui.sdk.SduiDataContext>(content.value)

        assertEquals(
            32149.0,
            (context["wallet.balance"] as SduiValue.NumberValue).value
        )

        val resolved = decodeLocalScreen(LocalScreens.wallet).resolveBindings(context)
        val textValues = resolved.allTextValues()

        assertEquals(true, textValues.contains("$32,149.00"))
        assertEquals(true, textValues.contains("Coffee shop"))
        assertEquals(true, textValues.contains("+ $2,500.00"))
    }

    private fun UiNode.allTextValues(): List<String> =
        buildList {
            (props["value"] as? SduiValue.StringValue)?.value?.let(::add)
            children.forEach { addAll(it.allTextValues()) }
        }
}
