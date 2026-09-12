package com.example.sdui.demo

import com.dhruti.sdui.sdk.FormState
import com.dhruti.sdui.sdk.SduiApiCallClient
import com.example.sdui.shared.UiAction

/** Local demo response: allowed API actions succeed without a network request. */
object LocalDemoApiCallClient : SduiApiCallClient {
    override suspend fun execute(action: UiAction, formState: FormState): Boolean = true
}
