package com.example.sdui.demo.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.sdui.demo.data.db.SduiDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(SduiDatabase.Schema, "sdui_cache.db")
    }
}
