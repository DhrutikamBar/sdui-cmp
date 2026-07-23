package com.example.sdui.app.db.composeApp

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.example.sdui.app.db.CachedScreenQueries
import com.example.sdui.app.db.SduiDatabase
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<SduiDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = SduiDatabaseImpl.Schema

internal fun KClass<SduiDatabase>.newInstance(driver: SqlDriver): SduiDatabase = SduiDatabaseImpl(driver)

private class SduiDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver),
    SduiDatabase {
  override val cachedScreenQueries: CachedScreenQueries = CachedScreenQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE CachedScreen (
          |    path TEXT NOT NULL PRIMARY KEY,
          |    content TEXT NOT NULL,
          |    updatedAt TEXT NOT NULL,
          |    lastAccessedAt INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
