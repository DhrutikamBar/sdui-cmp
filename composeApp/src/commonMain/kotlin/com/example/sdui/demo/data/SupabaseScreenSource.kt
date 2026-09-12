package com.example.sdui.demo.data

import com.example.sdui.shared.SduiDocumentCodec
import com.example.sdui.shared.UiNode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.protobuf.ProtoBuf
import io.github.jan.supabase.annotations.SupabaseInternal
import com.example.sdui.demo.data.db.SduiDatabase
import com.dhruti.sdui.sdk.ScreenLoadResult
import com.dhruti.sdui.sdk.ScreenLoadSource
import com.dhruti.sdui.sdk.ScreenRequest
import com.dhruti.sdui.sdk.ScreenSource

@OptIn(ExperimentalSerializationApi::class, SupabaseInternal::class)
class SupabaseScreenSource(
    private val supabaseUrl: String, 
    private val supabaseKey: String,
    driverFactory: DatabaseDriverFactory
) : ScreenSource {
    private companion object {
        const val CACHE_FORMAT_VERSION: Long = 2L
        const val CACHE_FRESHNESS_MILLIS = 15 * 60 * 1000L
    }

    private val database = SduiDatabase(driverFactory.createDriver())
    private val queries = database.cachedScreenQueries

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
            protobuf(ProtoBuf)
        }
        install(ContentEncoding) {
            gzip()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
            connectTimeoutMillis = 5000
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("KTOR: $message")
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { header -> header == "apikey" || header == "Authorization" }
        }
    }

    private val supabase = createSupabaseClient(supabaseUrl, supabaseKey) {
        install(Postgrest)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 5000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("SUPABASE: $message")
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    private val cache = mutableMapOf<String, UiNode>()
    private val prefetchJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Toggle between binary (Protobuf via Edge Functions) and JSON (Database).
     * Set to true only once Edge Functions are deployed.
     */
    var useBinaryTransport: Boolean = false

    /**
     * Hook for verifying payload signatures.
     * (rawBody, signatureHeader) -> Boolean
     */
    var signatureVerifier: ((String, String) -> Boolean)? = null

    private fun io.ktor.client.request.HttpRequestBuilder.authHeaders() {
        header("apikey", supabaseKey)
        header("Authorization", "Bearer $supabaseKey")
    }

    override suspend fun fetchScreen(path: String, forceRefresh: Boolean): UiNode =
        when (val result = loadScreen(ScreenRequest(path, forceRefresh))) {
            is ScreenLoadResult.Success -> result.screen
            is ScreenLoadResult.Failure -> throw result.cause
        }

    override suspend fun loadScreen(request: ScreenRequest): ScreenLoadResult = try {
        resolveScreen(request.path, request.forceRefresh)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (cause: Throwable) {
        ScreenLoadResult.Failure(cause)
    }

    private suspend fun resolveScreen(
        path: String,
        forceRefresh: Boolean
    ): ScreenLoadResult.Success {
        if (!forceRefresh) {
            cache[path]?.let { return ScreenLoadResult.Success(it, ScreenLoadSource.MEMORY) }
        }

        // Wait for an active prefetch before proceeding with cache resolution.
        try {
            withTimeout(5000) {
                prefetchJobs[path]?.join()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            println("KTOR: Prefetch join timed out for $path")
        }

        if (!forceRefresh) {
            cache[path]?.let { return ScreenLoadResult.Success(it, ScreenLoadSource.MEMORY) }
        }

        // Tier 2: check persistent cache and validate it when online.
        val persisted = queries.selectByPath(path).executeAsOneOrNull()
        if (!forceRefresh && persisted != null && persisted.formatVersion == CACHE_FORMAT_VERSION) {
            println("KTOR: [CACHE] Found disk entry for $path. Checking staleness...")
            try {
                val remoteUpdatedAt = fetchUpdatedAt(path)
                if (remoteUpdatedAt == persisted.updatedAt) {
                    println("KTOR: [CACHE] Disk entry is fresh. Loading from local DB.")
                    val contentNode = SduiDocumentCodec.decode(persisted.content).root
                    cache[path] = contentNode
                    queries.touchLastAccessed(getNowMillis(), path)
                    return ScreenLoadResult.Success(contentNode, ScreenLoadSource.DISK)
                } else {
                    println("KTOR: [CACHE] Disk entry is STALE. Remote: $remoteUpdatedAt, Local: ${persisted.updatedAt}")
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                println("KTOR: [CACHE] Network check failed. Falling back to disk entry for offline mode.")
                val contentNode = SduiDocumentCodec.decode(persisted.content).root
                cache[path] = contentNode
                return ScreenLoadResult.Success(contentNode, ScreenLoadSource.DISK)
            }
        } else if (persisted == null) {
            println("KTOR: [CACHE] No disk entry for $path. Will fetch from network.")
        }

        val row = fetchInternal(path)
        val screen = SduiDocumentCodec.decode(row.content).root
        cache[path] = screen

        // Persist asynchronously, as before.
        scope.launch { persistRow(path, row) }
        return ScreenLoadResult.Success(screen, ScreenLoadSource.NETWORK)
    }

    private suspend fun fetchUpdatedAt(path: String): String {
        return supabase.from("screens")
            .select(columns = Columns.list("updated_at")) {
                filter { eq("path", path) }
            }
            .decodeSingleOrNull<UpdatedAtRow>()?.updated_at 
            ?: throw NoSuchElementException("Screen not found in Supabase: $path")
    }

    private suspend fun fetchInternal(path: String): FullScreenRow {
        if (useBinaryTransport) {
            tryFetchBinary(path)?.let { return FullScreenRow(Json.encodeToJsonElement(UiNode.serializer(), it), "edge-function") }
        }
        return fetchFullRow(path)
    }

    private suspend fun fetchFullRow(path: String): FullScreenRow {
        return supabase.from("screens")
            .select(columns = Columns.list("content", "updated_at")) {
                filter { eq("path", path) }
            }
            .decodeSingleOrNull<FullScreenRow>()
            ?: throw NoSuchElementException("Screen not found in Supabase: $path")
    }

    private suspend fun tryFetchBinary(path: String): UiNode? {
        return try {
            val response = httpClient.get("${supabaseUrl}/functions/v1/sdui-binary") {
                parameter("path", path)
                authHeaders()
                contentType(ContentType.Application.ProtoBuf)
            }
            
            if (!response.status.isSuccess()) {
                println("KTOR: Binary fetch skipped (Status: ${response.status})")
                return null
            }

            val bytes = response.body<ByteArray>()
            
            signatureVerifier?.let { verify ->
                val signature = response.headers["X-UI-Signature"] ?: ""
                val bodyText = bytes.decodeToString()
                if (!verify(bodyText, signature)) throw Exception("Invalid UI signature")
            }

            ProtoBuf.decodeFromByteArray(UiNode.serializer(), bytes)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (e: Exception) {
            println("KTOR: Binary fetch error: ${e.message}")
            null
        }
    }

    override fun prefetch(path: String) {
        if (cache.containsKey(path) || prefetchJobs.containsKey(path)) return

        val job = scope.launch {
            try {
                val row = fetchInternal(path)
                cache[path] = SduiDocumentCodec.decode(row.content).root
                persistRow(path, row)
            } catch (e: Exception) {
                // Best-effort
            } finally {
                prefetchJobs.remove(path)
            }
        }
        prefetchJobs[path] = job
    }

    override fun cancelPrefetch(path: String) {
        prefetchJobs.remove(path)?.cancel()
    }

    override fun close() {
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
        scope.cancel()
        httpClient.close()
    }

    private fun persistRow(path: String, row: FullScreenRow) {
        try {
            queries.upsert(
                path = path,
                content = SduiDocumentCodec.encode(SduiDocumentCodec.decode(row.content)),
                updatedAt = row.updated_at,
                lastAccessedAt = getNowMillis(),
                formatVersion = CACHE_FORMAT_VERSION,
                cachedAt = getNowMillis()
            )
            enforceEvictionLimit()
        } catch (e: Exception) {
            println("KTOR: Persist failed for $path: ${e.message}")
        }
    }

    private fun enforceEvictionLimit() {
        val count = queries.countAll().executeAsOne()
        if (count > 200) {
            queries.deleteLeastRecentlyUsed(count - 200)
        }
    }

    fun clearCache() {
        cache.clear()
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
        queries.deleteAll()
    }
}

private fun io.ktor.client.request.HttpRequestBuilder.parameter(key: String, value: String) {
    url.parameters.append(key, value)
}

@Serializable
data class UpdatedAtRow(val updated_at: String)

@Serializable
data class FullScreenRow(val content: JsonElement, val updated_at: String)
