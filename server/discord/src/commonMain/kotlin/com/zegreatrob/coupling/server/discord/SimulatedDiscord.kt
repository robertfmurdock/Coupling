package com.zegreatrob.coupling.server.discord

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.parseUrlEncodedParameters
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun simulatedDiscordHttpClient() = HttpClient(
    MockEngine { request ->
        when {
            request.method == HttpMethod.Post && request.url.encodedPath == "/api/oauth2/token" -> exchange(request)
            request.url.encodedPath.startsWith("/api/webhooks/") -> webhook(request)
            else -> errorResponse("Unknown simulated Discord endpoint.", HttpStatusCode.NotFound)
        }
    },
)

private suspend fun MockRequestHandleScope.exchange(request: HttpRequestData) = request.body.toByteArray()
    .decodeToString().parseUrlEncodedParameters()["code"]
    ?.let { Regex("simulated:([A-Za-z0-9_-]+)").matchEntire(it)?.groupValues?.get(1) }
    ?.let { destination ->
        respond(
            Json.encodeToString(destination.accessResponse()),
            headers = jsonHeaders,
        )
    } ?: errorResponse("Expected simulated:<destination>.", HttpStatusCode.BadRequest)

private fun String.accessResponse() = SuccessfulAccessResponse(
    accessToken = "simulated-access-$this",
    tokenType = "Bearer",
    expiresIn = 3600,
    refreshToken = "simulated-refresh-$this",
    scope = "webhook.incoming",
    webhook = WebhookInformation(
        token = "simulated-token-$this",
        id = "simulated-webhook-$this",
        applicationId = "simulated-app",
        name = "Simulated Discord",
        url = "https://discord.invalid/api/webhooks/simulated-webhook-$this/simulated-token-$this",
        channelId = this,
        type = 1,
        avatar = null,
        guildId = "simulated-guild-$this",
    ),
)

private suspend fun MockRequestHandleScope.webhook(request: HttpRequestData): HttpResponseData {
    val path = request.url.encodedPath.removePrefix("/api/webhooks/").split("/")
    val destination = path.first().removePrefix("simulated-webhook-")
    if (path.first() != "simulated-webhook-$destination" || path.getOrNull(1) != "simulated-token-$destination") {
        return errorResponse("Unknown simulated webhook.", HttpStatusCode.NotFound)
    }
    return when {
        path.size == 2 && request.method == HttpMethod.Post -> messageResponse(request, path.first(), destination, null)
        path.size == 4 && path[2] == "messages" && request.method == HttpMethod.Patch -> messageResponse(request, path.first(), destination, path[3])
        path.size == 4 && path[2] == "messages" && request.method == HttpMethod.Delete -> respond("", HttpStatusCode.NoContent)
        else -> errorResponse("Unknown simulated webhook operation.", HttpStatusCode.NotFound)
    }
}

private suspend fun MockRequestHandleScope.messageResponse(request: HttpRequestData, webhookId: String, destination: String, messageId: String?) = request.body
    .toByteArray().decodeToString().let { body ->
        val payload = Json.parseToJsonElement(body).jsonObject
        respond(
            Json.encodeToString(
                MessageResponseData(
                    id = messageId ?: "simulated-message-${"$webhookId:$body".hashCode().toUInt().toString(16)}",
                    type = 0,
                    content = payload.getValue("content").jsonPrimitive.content,
                    channelId = destination,
                    author = MessageAuthor(true, "simulated-app", "Simulated Discord", null, null),
                    attachments = emptyList(),
                    embeds = payload.getValue("embeds").jsonArray.toList(),
                    mentions = emptyList(),
                    mentionRoles = emptyList(),
                    pinned = false,
                    mentionEveryone = false,
                    tts = false,
                    timestamp = "2000-01-01T00:00:00.000Z",
                    editedTimestamp = null,
                    flags = 0,
                    components = emptyList(),
                    applicationId = "simulated-app",
                    webhookId = webhookId,
                ),
            ),
            headers = jsonHeaders,
        )
    }

private fun MockRequestHandleScope.errorResponse(description: String, status: HttpStatusCode) = respond(
    Json.encodeToString(ErrorAccessResponse(error = "invalid_grant", errorDescription = description)),
    status,
    jsonHeaders,
)

private val jsonHeaders = headersOf("Content-Type", "application/json")
