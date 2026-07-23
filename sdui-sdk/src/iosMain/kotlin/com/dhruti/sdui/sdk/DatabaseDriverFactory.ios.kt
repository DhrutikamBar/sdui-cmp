package com.dhruti.sdui.sdk

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.dhruti.sdui.sdk.db.SduiDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(SduiDatabase.Schema, "sdui_cache.db")
    }
}
