package com.example.sdui.app

import com.example.sdui.shared.UiNode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Manages fetching and caching of SDUI screens.
 */
@OptIn(ExperimentalSerializationApi::class)
class UiRepository(val baseUrl: String) {
    private val cache = mutableMapOf<String, UiNode>()
    private val prefetchJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Optional hook for verifying payload signatures.
     * Arguments: (rawBody, signatureHeader) -> Boolean
     */
    var signatureVerifier: ((String, String) -> Boolean)? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
            protobuf(ProtoBuf)
        }
    }

    suspend fun fetchScreen(path: String, forceRefresh: Boolean = false): UiNode {
        return fetchInternal(path, forceRefresh, usePrefetchLock = true)
    }

    private suspend fun fetchInternal(path: String, forceRefresh: Boolean, usePrefetchLock: Boolean): UiNode {
        if (!forceRefresh && cache.containsKey(path)) {
            return cache[path]!!
        }

        if (usePrefetchLock) {
            prefetchJobs[path]?.join()
        }

        if (!forceRefresh && cache.containsKey(path)) {
            return cache[path]!!
        }

        try {
            val response: HttpResponse = client.get(baseUrl + path) {
                contentType(ContentType.Application.ProtoBuf)
            }

            val screen: UiNode = response.body()
            cache[path] = screen
            return screen
        } catch (e: Exception) {
            throw e
        }
    }

    fun prefetch(path: String) {
        if (cache.containsKey(path) || prefetchJobs.containsKey(path)) return

        val job = scope.launch {
            try {
                fetchInternal(path, forceRefresh = false, usePrefetchLock = false)
            } catch (e: Exception) {
                // Prefetch failed
            } finally {
                prefetchJobs.remove(path)
            }
        }
        prefetchJobs[path] = job
    }

    fun clearCache() {
        cache.clear()
        prefetchJobs.values.forEach { it.cancel() }
        prefetchJobs.clear()
    }
}
