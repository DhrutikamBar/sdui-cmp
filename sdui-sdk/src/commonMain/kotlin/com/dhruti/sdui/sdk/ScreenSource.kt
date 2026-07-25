package com.dhruti.sdui.sdk

import com.example.sdui.shared.UiNode

interface ScreenSource {
    suspend fun fetchScreen(path: String, forceRefresh: Boolean = false): UiNode
    fun prefetch(path: String)
}
