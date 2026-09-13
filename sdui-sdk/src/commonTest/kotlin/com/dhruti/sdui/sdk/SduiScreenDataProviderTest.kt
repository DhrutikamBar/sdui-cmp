package com.dhruti.sdui.sdk

import kotlin.test.Test
import kotlin.test.assertIs

class SduiScreenDataProviderTest {
    @Test
    fun defaultProviderPreservesStaticScreenCompatibility() {
        val state = EmptySduiScreenDataProvider.stateFor("any-route").value

        assertIs<SduiDataState.Content<SduiDataContext>>(state)
    }
}
