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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Manages fetching and caching of SDUI screens.
 */
@OptIn(ExperimentalSerializationApi::class)
class UiRepository(val baseUrl: String) {
    private val cache = mutableMapOf<String, UiNode>()
    
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
        if (!forceRefresh && cache.containsKey(path)) {
            return cache[path]!!
        }
        
        try {
            // Request Protobuf preferred, fallback to JSON
            val screen: UiNode = client.get(baseUrl + path) {
                contentType(ContentType.Application.ProtoBuf)
            }.body()
            
            cache[path] = screen
            return screen
        } catch (e: Exception) {
            // Failure is handled in App.kt (LaunchedEffect), but we could log it here too
            throw e
        }
    }
    
    fun clearCache() {
        cache.clear()
    }
}
