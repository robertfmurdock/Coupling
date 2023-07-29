package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class DiscordClient(
    private val clientId: String,
    private val clientSecret: String,
    private val host: String,
    httpClient: HttpClient = HttpClient(),
) {
    private val httpClient = httpClient.config {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        defaultRequest {
            url("https://discord.com/api/")
        }
    }

    suspend fun getAccessToken(code: String): AccessResponse {
        val response = httpClient.submitForm(
            "oauth2/token",
            Parameters.build {
                append("grant_type", "authorization_code")
                append("code", code)
                append("redirect_uri", "$host/api/discord")
            },
        ) {
            basicAuth(clientId, clientSecret)
        }
        return if (response.status.isSuccess()) {
            response.body<SuccessfulAccessResponse>()
        } else {
            response.body<ErrorAccessResponse>()
        }
    }
}

sealed interface AccessResponse

@Serializable
data class SuccessfulAccessResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("refresh_token")
    val refreshToken: String,
    val scope: String,
    val webhook: WebhookInformation,
) : AccessResponse

@Serializable
data class ErrorAccessResponse(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String? = null,
) : AccessResponse

@Serializable
data class WebhookInformation(
    val token: String,
    val id: String,
    @SerialName("application_id")
    val applicationId: String,
    val name: String?,
    val url: String,
    @SerialName("channel_id")
    val channelId: String,
    val type: Int,
    val avatar: String?,
    @SerialName("guild_id")
    val guildId: String?,
)
