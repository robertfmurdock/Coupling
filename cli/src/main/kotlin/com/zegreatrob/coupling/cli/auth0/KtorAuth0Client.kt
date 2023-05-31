package com.zegreatrob.coupling.cli.auth0

import com.zegreatrob.coupling.cli.Auth0Environment
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
    val auth0HttpClient = HttpClient {
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
        environment: Auth0Environment,
        deviceCode: String,
    ) = auth0HttpClient.submitForm(
        url = "https://$AUTH0_DOMAIN/oauth/token",
        formParameters = Parameters.build {
            append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
            append("client_id", environment.clientId)
            append("device_code", deviceCode)
        },
    ).body<PollResult>()

    suspend fun getDeviceCodeRequest(environment: Auth0Environment): DeviceCodeRequest {
        val result = auth0HttpClient.submitForm(
            url = "https://$AUTH0_DOMAIN/oauth/device/code",
            formParameters = Parameters.build {
                append("audience", environment.audience)
                append("scope", "email, offline_access")
                append("client_id", environment.clientId)
            },
        ).body<DeviceCodeRequest>()
        return result
    }
}
