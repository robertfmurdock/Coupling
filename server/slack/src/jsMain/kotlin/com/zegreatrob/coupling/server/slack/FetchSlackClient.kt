package com.zegreatrob.coupling.server.slack

import js.array.tupleOf
import js.objects.jso
import js.objects.recordOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import web.form.FormData
import web.http.BodyInit
import web.http.Headers
import web.http.Request
import web.http.fetch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json

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
            headers = Headers(recordOf("Authorization" to "Basic ${btoa("$clientId:$clientSecret")}"))
            body = BodyInit(
                FormData().apply {
                    append("code", code)
                    append("redirect_uri", slackRedirectUri)
                },
            )
        },
    )
        .textAsync()
        .await()
        .let(jsonParser::decodeFromString)

    @ExperimentalEncodingApi
    fun btoa(s: String): String = Base64.encode(s.encodeToByteArray())

    override suspend fun postMessage(
        text: String,
        channel: String,
        accessToken: String,
        blocks: String?,
    ): MessageResponse {
        val request = Request(
            "https://slack.com/api/chat.postMessage",
            jso {
                method = "post"
                headers = jsonHeaders(accessToken)
                body = BodyInit(
                    JSON.stringify(
                        json(
                            "channel" to channel,
                            "text" to text,
                            "blocks" to blocks,
                        ),
                    ),
                )
            },
        )
        console.log("FETCH SLACK", JSON.stringify(request))
        return fetch(request)
            .textAsync()
            .await()
            .let(jsonParser::decodeFromString)
    }

    private fun jsonHeaders(accessToken: String) = Headers(
        arrayOf(
            tupleOf("Authorization", "Bearer $accessToken"),
            tupleOf("Content-Type", "application/json"),
        ),
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
            body = BodyInit(
                JSON.stringify(
                    json(
                        "ts" to ts,
                        "channel" to channel,
                        "text" to text,
                        "blocks" to blocks,
                    ),
                ),
            )
        },
    )
        .textAsync()
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
            body = BodyInit(
                JSON.stringify(
                    json(
                        "channel" to channel,
                        "latest" to latest.toUnixString(),
                        "oldest" to oldest.toUnixString(),
                    ),
                ),
            )
        },
    )
        .textAsync()
        .await()
        .let(jsonParser::decodeFromString)

    suspend fun deleteMessage(accessToken: String, channel: String, ts: String): MessageResponse = fetch(
        "https://slack.com/api/chat.delete",
        jso {
            method = "post"
            headers = jsonHeaders(accessToken)
            body = BodyInit(
                JSON.stringify(
                    json(
                        "ts" to ts,
                        "channel" to channel,
                    ),
                ),
            )
        },
    )
        .textAsync()
        .await()
        .let(jsonParser::decodeFromString)
}

private fun Double.toUnixString(): String = "${this / 1_000}"

@Serializable
data class AccessResponse(
    val ok: Boolean? = null,
    val error: String? = null,
    val team: AccessResponseTeam? = null,
    @SerialName("bot_user_id")
    val botUserId: String? = null,
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
