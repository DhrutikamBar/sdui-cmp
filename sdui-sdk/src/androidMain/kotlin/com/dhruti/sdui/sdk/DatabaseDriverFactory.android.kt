package com.dhruti.sdui.sdk

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.dhruti.sdui.sdk.db.SduiDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(SduiDatabase.Schema, context, "sdui_cache.db")
    }
}
