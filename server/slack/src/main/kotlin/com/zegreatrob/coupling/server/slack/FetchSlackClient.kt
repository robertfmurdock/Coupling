package com.zegreatrob.coupling.server.slack

import js.core.jso
import kotlinx.coroutines.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import web.http.FormData
import web.http.fetch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Json
import kotlin.js.json

interface SlackClient {
    suspend fun exchangeCodeForAccess(code: String): AccessResponse
    suspend fun sendMessage(message: String, channel: String, accessToken: String)
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

    override suspend fun sendMessage(message: String, channel: String, accessToken: String) {
        fetch(
            "https://slack.com/api/chat.postMessage",
            jso {
                method = "post"
                headers = json(
                    "Authorization" to "Bearer $accessToken",
                    "Content-type" to "application/json",
                )
                body = JSON.stringify(
                    json(
                        "channel" to channel,
                        "text" to message,
                    ),
                )
            },
        )
            .text()
            .await()
            .let { JSON.parse<Json>(it) }
            .let {
                if (it["ok"] != false) {
                    it
                } else {
                    throw Exception("${it["error"]}")
                }
            }
    }
}

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
data class AccessResponseTeam(
    val id: String,
)

@Serializable
data class AuthedUser(
    val id: String,
)
