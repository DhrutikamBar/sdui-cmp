package com.example.sdui.app.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.sdui.app.db.composeApp.newInstance
import com.example.sdui.app.db.composeApp.schema
import kotlin.Unit

public interface SduiDatabase : Transacter {
  public val cachedScreenQueries: CachedScreenQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = SduiDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): SduiDatabase = SduiDatabase::class.newInstance(driver)
  }
}
