package com.example.sdui.app.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class CachedScreenQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectByPath(path: String, mapper: (
    path: String,
    content: String,
    updatedAt: String,
    lastAccessedAt: Long,
  ) -> T): Query<T> = SelectByPathQuery(path) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!
    )
  }

  public fun selectByPath(path: String): Query<CachedScreen> = selectByPath(path, ::CachedScreen)

  public fun countAll(): Query<Long> = Query(-388_582_587, arrayOf("CachedScreen"), driver, "CachedScreen.sq", "countAll", "SELECT COUNT(*) FROM CachedScreen") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun upsert(
    path: String,
    content: String,
    updatedAt: String,
    lastAccessedAt: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_646_500_642, """
        |INSERT OR REPLACE INTO CachedScreen(path, content, updatedAt, lastAccessedAt)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          var parameterIndex = 0
          bindString(parameterIndex++, path)
          bindString(parameterIndex++, content)
          bindString(parameterIndex++, updatedAt)
          bindLong(parameterIndex++, lastAccessedAt)
        }
    notifyQueries(1_646_500_642) { emit ->
      emit("CachedScreen")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun touchLastAccessed(lastAccessedAt: Long, path: String): QueryResult<Long> {
    val result = driver.execute(-1_574_494_523, """UPDATE CachedScreen SET lastAccessedAt = ? WHERE path = ?""", 2) {
          var parameterIndex = 0
          bindLong(parameterIndex++, lastAccessedAt)
          bindString(parameterIndex++, path)
        }
    notifyQueries(-1_574_494_523) { emit ->
      emit("CachedScreen")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteLeastRecentlyUsed(n: Long): QueryResult<Long> {
    val result = driver.execute(1_324_132_880, """
        |DELETE FROM CachedScreen WHERE path IN (
        |  SELECT path FROM CachedScreen ORDER BY lastAccessedAt ASC LIMIT ?
        |)
        """.trimMargin(), 1) {
          var parameterIndex = 0
          bindLong(parameterIndex++, n)
        }
    notifyQueries(1_324_132_880) { emit ->
      emit("CachedScreen")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAll(): QueryResult<Long> {
    val result = driver.execute(-871_076_573, """DELETE FROM CachedScreen""", 0)
    notifyQueries(-871_076_573) { emit ->
      emit("CachedScreen")
    }
    return result
  }

  private inner class SelectByPathQuery<out T : Any>(
    public val path: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("CachedScreen", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("CachedScreen", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> = driver.executeQuery(-1_503_963_925, """SELECT CachedScreen.path, CachedScreen.content, CachedScreen.updatedAt, CachedScreen.lastAccessedAt FROM CachedScreen WHERE path = ?""", mapper, 1) {
      var parameterIndex = 0
      bindString(parameterIndex++, path)
    }

    override fun toString(): String = "CachedScreen.sq:selectByPath"
  }
}
