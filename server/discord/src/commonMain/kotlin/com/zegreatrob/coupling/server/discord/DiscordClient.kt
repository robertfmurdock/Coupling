package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

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
                append("redirect_uri", "$host/integration/discord/callback")
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

    suspend fun sendWebhookMessage(
        message: String,
        webhookId: String,
        webhookToken: String,
        embeds: List<DiscordEmbed>,
    ): MessageResponse {
        val response = httpClient.post(
            "webhooks/$webhookId/$webhookToken?wait=true",
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("content", message)
                    putJsonArray("embeds") {
                        embeds.forEach { embed ->
                            addJsonObject {
                                put("title", embed.title)
                                put("description", embed.description)
                                if (embed.imageUrl != null) {
                                    putJsonObject("image") {
                                        put("url", embed.imageUrl)
                                    }
                                }
                            }
                        }
                    }
                },
            )
        }
        return if (response.status.isSuccess()) {
            response.body<MessageResponseData>()
        } else {
            response.body<ErrorAccessResponse>()
        }
    }

    suspend fun updateWebhookMessage(
        messageId: String,
        message: String,
        webhookId: String,
        webhookToken: String,
    ): MessageResponse {
        val response = httpClient.patch(
            "webhooks/$webhookId/$webhookToken/messages/$messageId",
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("content", message)
                },
            )
        }
        return if (response.status.isSuccess()) {
            response.body<MessageResponseData>()
        } else {
            response.body<ErrorAccessResponse>()
        }
    }
}

data class DiscordEmbed(
    val title: String,
    val description: String,
    val imageUrl: String?,
)
