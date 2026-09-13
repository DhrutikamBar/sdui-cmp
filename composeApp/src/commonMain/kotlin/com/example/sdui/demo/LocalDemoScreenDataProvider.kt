package com.example.sdui.demo

import com.dhruti.sdui.sdk.SduiDataContext
import com.dhruti.sdui.sdk.SduiDataState
import com.dhruti.sdui.sdk.SduiScreenDataProvider
import com.example.sdui.shared.SduiValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Demo-only stand-in for an authenticated business API. A production host maps
 * its response models into the same [SduiDataContext] shape.
 */
class LocalDemoScreenDataProvider : SduiScreenDataProvider {
    private val states = mutableMapOf<String, MutableStateFlow<SduiDataState<SduiDataContext>>>()

    override fun stateFor(path: String): StateFlow<SduiDataState<SduiDataContext>> =
        states.getOrPut(path) {
            MutableStateFlow(SduiDataState.Content(contextFor(path)))
        }

    override suspend fun refresh(path: String) {
        val state = states.getOrPut(path) {
            MutableStateFlow(SduiDataState.Content(contextFor(path)))
        }
        state.value = SduiDataState.Loading
        delay(350)
        state.value = SduiDataState.Content(contextFor(path))
    }

    private fun contextFor(path: String): SduiDataContext = SduiDataContext(
        mapOf(
            "screen" to SduiValue.StringValue(path),
            "user" to SduiValue.ObjectValue(
                mapOf(
                    "name" to SduiValue.StringValue("Tanjiro Kamado"),
                    "firstName" to SduiValue.StringValue("Tanjiro")
                )
            ),
            "wallet" to SduiValue.ObjectValue(
                mapOf(
                    "accountName" to SduiValue.StringValue("Everyday account"),
                    "balance" to SduiValue.NumberValue(32149.0),
                    "balanceDisplay" to SduiValue.StringValue("$32,149.00"),
                    "currency" to SduiValue.StringValue("USD")
                )
            ),
            "transactions" to SduiValue.ListValue(
                listOf(
                    transaction("Coffee shop", "- $4.50", "#B3261E"),
                    transaction("Salary", "+ $2,500.00", "#2E7D32"),
                    transaction("Streaming subscription", "- $12.99", "#B3261E")
                )
            )
        )
    )

    private fun transaction(title: String, amount: String, color: String): SduiValue.ObjectValue =
        SduiValue.ObjectValue(
            mapOf(
                "title" to SduiValue.StringValue(title),
                "amountDisplay" to SduiValue.StringValue(amount),
                "amountColor" to SduiValue.StringValue(color)
            )
        )
}
