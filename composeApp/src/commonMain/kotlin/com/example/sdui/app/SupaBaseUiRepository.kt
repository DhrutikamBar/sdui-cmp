package com.example.sdui.app

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
import kotlinx.serialization.protobuf.ProtoBuf
import io.github.jan.supabase.annotations.SupabaseInternal
import com.dhruti.sdui.sdk.db.SduiDatabase
import com.dhruti.sdui.sdk.DatabaseDriverFactory
import kotlinx.datetime.Clock

@OptIn(ExperimentalSerializationApi::class, SupabaseInternal::class)
class SupaBaseUiRepository(
    private val supabaseUrl: String, 
    private val supabaseKey: String,
    driverFactory: DatabaseDriverFactory
) {
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

    suspend fun fetchScreen(path: String, forceRefresh: Boolean = false): UiNode {
        if (!forceRefresh && cache.containsKey(path)) {
            return cache[path]!!
        }

        // Concurrency: Wait for active prefetch if it exists
        try {
            withTimeout(5000) {
                prefetchJobs[path]?.join()
            }
        } catch (e: Exception) {
            println("KTOR: Prefetch join timed out for $path")
        }

        if (!forceRefresh && cache.containsKey(path)) {
            return cache[path]!!
        }

        // Tier 2: Check Disk
        val persisted = queries.selectByPath(path).executeAsOneOrNull()
        if (!forceRefresh && persisted != null) {
            try {
                val remoteUpdatedAt = fetchUpdatedAt(path)
                if (remoteUpdatedAt == persisted.updatedAt) {
                    val contentNode = Json.decodeFromString(UiNode.serializer(), persisted.content)
                    cache[path] = contentNode
                    queries.touchLastAccessed(Clock.System.now().toEpochMilliseconds(), path)
                    return contentNode
                }
            } catch (e: Exception) {
                val contentNode = Json.decodeFromString(UiNode.serializer(), persisted.content)
                cache[path] = contentNode
                return contentNode
            }
        }

        return try {
            val row = fetchInternal(path)
            val screen = row.content
            cache[path] = screen
            
            // Persist in background
            scope.launch { persistRow(path, row) }
            
            screen
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun fetchUpdatedAt(path: String): String {
        return supabase.from("screens")
            .select(columns = Columns.list("updated_at")) {
                filter { eq("path", path) }
            }
            .decodeSingle<UpdatedAtRow>()
            .updated_at
    }

    private suspend fun fetchInternal(path: String): FullScreenRow {
        if (useBinaryTransport) {
            tryFetchBinary(path)?.let { return FullScreenRow(it, "edge-function") }
        }
        return fetchFullRow(path)
    }

    private suspend fun fetchFullRow(path: String): FullScreenRow {
        return supabase.from("screens")
            .select(columns = Columns.list("content", "updated_at")) {
                filter { eq("path", path) }
            }
            .decodeSingle<FullScreenRow>()
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
        } catch (e: Exception) {
            println("KTOR: Binary fetch error: ${e.message}")
            null
        }
    }

    fun prefetch(path: String) {
        if (cache.containsKey(path) || prefetchJobs.containsKey(path)) return

        val job = scope.launch {
            try {
                val row = fetchInternal(path)
                cache[path] = row.content
                persistRow(path, row)
            } catch (e: Exception) {
                // Best-effort
            } finally {
                prefetchJobs.remove(path)
            }
        }
        prefetchJobs[path] = job
    }

    private fun persistRow(path: String, row: FullScreenRow) {
        try {
            queries.upsert(
                path = path,
                content = Json.encodeToString(UiNode.serializer(), row.content),
                updatedAt = row.updated_at,
                lastAccessedAt = Clock.System.now().toEpochMilliseconds()
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
data class FullScreenRow(val content: UiNode, val updated_at: String)
