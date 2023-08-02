package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
            json(json = Json { ignoreUnknownKeys = true })
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
            webhookMessageRequest(message, embeds),
        )
        return if (response.status.isSuccess()) {
            response.body<MessageResponseData>()
        } else {
            response.body<ErrorAccessResponse>()
        }
    }

    private fun webhookMessageRequest(message: String, embeds: List<DiscordEmbed>): HttpRequestBuilder.() -> Unit = {
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

    suspend fun updateWebhookMessage(
        messageId: String,
        message: String,
        webhookId: String,
        webhookToken: String,
        embeds: List<DiscordEmbed>,
    ): MessageResponse {
        val response = httpClient.patch(
            "webhooks/$webhookId/$webhookToken/messages/$messageId",
            webhookMessageRequest(message, embeds),
        )
        return if (response.status.isSuccess()) {
            response.body<MessageResponseData>()
        } else {
            response.body<ErrorAccessResponse>()
        }
    }

    suspend fun deleteWebhookMessage(messageId: String, webhookId: String, webhookToken: String): Boolean =
        httpClient.delete("webhooks/$webhookId/$webhookToken/messages/$messageId")
            .status.isSuccess()
}

data class DiscordEmbed(
    val title: String,
    val description: String,
    val imageUrl: String?,
)
