package com.zegreatrob.coupling.server.slack

import js.core.jso
import kotlinx.coroutines.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import web.http.FormData
import web.http.fetch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json

interface SlackClient {
    suspend fun exchangeCodeForAccess(code: String): AccessResponse

    suspend fun postMessage(
        text: String,
        channel: String,
        accessToken: String,
        blocks: String? = null,
    ): MessageResponse

    suspend fun updateMessage(
        accessToken: String,
        channel: String,
        ts: String?,
        text: String,
        blocks: String? = null,
    ): MessageResponse

    suspend fun getConversationHistory(
        accessToken: String,
        channel: String,
        latest: Double,
        oldest: Double,
    ): HistoryResponse
}

@ExperimentalEncodingApi
class FetchSlackClient(
    private val clientId: String,
    private val clientSecret: String,
    private val slackRedirectUri: String,
) : SlackClient {

    private val jsonParser = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
    }

    override suspend fun exchangeCodeForAccess(code: String): AccessResponse = fetch(
        "https://slack.com/api/oauth.v2.access",
        jso {
            method = "post"
            headers = json("Authorization" to "Basic ${btoa("$clientId:$clientSecret")}")
            body = FormData().apply {
                append("code", code)
                append("redirect_uri", slackRedirectUri)
            }
        },
    )
        .text()
        .await()
        .let(jsonParser::decodeFromString)

    @ExperimentalEncodingApi
    fun btoa(s: String): String = Base64.encode(s.encodeToByteArray())

    override suspend fun postMessage(
        text: String,
        channel: String,
        accessToken: String,
        blocks: String?,
    ): MessageResponse = fetch(
        "https://slack.com/api/chat.postMessage",
        jso {
            method = "post"
            headers = jsonHeaders(accessToken)
            body = JSON.stringify(
                json(
                    "channel" to channel,
                    "text" to text,
                    "blocks" to blocks,
                ),
            )
        },
    )
        .text()
        .await()
        .let(jsonParser::decodeFromString)

    private fun jsonHeaders(accessToken: String) = json(
        "Authorization" to "Bearer $accessToken",
        "Content-type" to "application/json",
    )

    override suspend fun updateMessage(
        accessToken: String,
        channel: String,
        ts: String?,
        text: String,
        blocks: String?,
    ): MessageResponse = fetch(
        "https://slack.com/api/chat.update",
        jso {
            method = "post"
            headers = jsonHeaders(accessToken)
            body = JSON.stringify(
                json(
                    "ts" to ts,
                    "channel" to channel,
                    "text" to text,
                    "blocks" to blocks,
                ),
            )
        },
    )
        .text()
        .await()
        .let(jsonParser::decodeFromString)

    override suspend fun getConversationHistory(
        accessToken: String,
        channel: String,
        latest: Double,
        oldest: Double,
    ): HistoryResponse = fetch(
        "https://slack.com/api/conversations.history",
        jso {
            method = "post"
            headers = jsonHeaders(accessToken)
            body = JSON.stringify(
                json(
                    "channel" to channel,
                    "latest" to latest.toUnixString(),
                    "oldest" to oldest.toUnixString(),
                ),
            )
        },
    )
        .text()
        .await()
        .let(jsonParser::decodeFromString)
}

private fun Double.toUnixString(): String = "${this / 1_000}"

@Serializable
data class AccessResponse(
    val ok: Boolean? = null,
    val error: String? = null,
    val team: AccessResponseTeam? = null,
    @SerialName("authed_user")
    val authedUser: AuthedUser? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("app_id")
    val appId: String? = null,
)

@Serializable
data class MessageResponse(
    val ok: Boolean? = null,
    val error: String? = null,
    val ts: String? = null,
)

@Serializable
data class HistoryResponse(
    val ok: Boolean? = null,
    val error: String? = null,
    val messages: List<MessageReference>? = null,
)

@Serializable
data class MessageReference(
    val type: String,
    val user: String,
    val text: String,
    val blocks: JsonElement? = null,
    val ts: String,
)

@Serializable
data class AccessResponseTeam(
    val id: String,
)

@Serializable
data class AuthedUser(
    val id: String,
)
