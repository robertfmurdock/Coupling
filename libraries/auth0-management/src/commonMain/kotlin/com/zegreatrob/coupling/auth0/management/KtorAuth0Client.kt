package com.zegreatrob.coupling.auth0.management

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val AUTH0_DOMAIN = "zegreatrob.us.auth0.com"

class KtorAuth0Client {
    private val auth0HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        Logging { level = LogLevel.NONE }
    }

    suspend fun checkForResult(
        clientId: String,
        deviceCode: String,
    ) = auth0HttpClient.submitForm(
        url = "https://$AUTH0_DOMAIN/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            append("client_id", clientId)
            append("device_code", deviceCode)
        },
    ).body<PollResult>()

    suspend fun getDeviceCodeRequest(audience: String, clientId: String): DeviceCodeRequest {
        val result = auth0HttpClient.submitForm(
            url = "https://$AUTH0_DOMAIN/oauth/device/code",
            formParameters = Parameters.build {
                append("audience", audience)
                append("scope", "email, offline_access")
                append("client_id", clientId)
            },
        ).body<DeviceCodeRequest>()
        return result
    }
}
