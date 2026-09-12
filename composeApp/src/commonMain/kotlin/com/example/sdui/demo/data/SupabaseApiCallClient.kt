package com.example.sdui.demo.data

import com.dhruti.sdui.sdk.FormState
import com.dhruti.sdui.sdk.SduiApiCallClient
import com.dhruti.sdui.sdk.interpolate
import com.example.sdui.shared.UiAction
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Reference-host implementation of [SduiApiCallClient] for Supabase endpoints.
 *
 * The action policy must validate the method and relative target before this
 * client is invoked.
 */
class SupabaseApiCallClient(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String
) : SduiApiCallClient {
    override suspend fun execute(action: UiAction, formState: FormState): Boolean {
        val target = action.target ?: return false
        val response = httpClient.request(supabaseUrl + target) {
            method = HttpMethod.parse(action.method ?: "POST")
            header("apikey", supabaseKey)
            header("Authorization", "Bearer $supabaseKey")
            action.body?.let { body ->
                contentType(ContentType.Application.Json)
                setBody(interpolate(body, formState))
            }
        }
        return response.status.isSuccess()
    }
}
