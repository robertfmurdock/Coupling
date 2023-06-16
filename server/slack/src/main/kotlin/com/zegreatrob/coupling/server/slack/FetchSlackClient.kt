@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.server.slack

import js.core.jso
import kotlinx.coroutines.await
import web.http.FormData
import web.http.fetch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.Json
import kotlin.js.json

interface SlackClient {
    suspend fun exchangeCodeForAccess(code: String): dynamic
    suspend fun sendMessage(message: String, channel: String, accessToken: String)
}

class FetchSlackClient(
    private val clientId: String,
    private val clientSecret: String,
    private val slackRedirectUri: String,
) : SlackClient {

    override suspend fun exchangeCodeForAccess(code: String): dynamic = fetch(
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
        .let { JSON.parse<Json>(it) }
        .let {
            if (it["ok"] != false) {
                it
            } else {
                throw Exception("${it["error"]}")
            }
        }

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
