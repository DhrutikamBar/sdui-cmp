package com.example.sdui.app.db

import kotlin.Long
import kotlin.String

public data class CachedScreen(
  public val path: String,
  public val content: String,
  public val updatedAt: String,
  public val lastAccessedAt: Long,
)
