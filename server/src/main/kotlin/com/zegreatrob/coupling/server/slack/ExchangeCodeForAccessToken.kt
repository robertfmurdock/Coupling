@file:OptIn(ExperimentalEncodingApi::class)

package com.zegreatrob.coupling.server.slack

import com.zegreatrob.coupling.server.btoa
import com.zegreatrob.coupling.server.express.Config
import js.core.jso
import kotlinx.coroutines.await
import web.http.FormData
import web.http.fetch
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.json

suspend fun exchangeCodeForAccessToken(code: String): dynamic = fetch(
    "https://slack.com/api/oauth.v2.access",
    jso {
        method = "post"
        headers = json("Authorization" to "Basic ${btoa("${Config.slackClientId}:${Config.slackClientSecret}")}")
        body = FormData().apply {
            append("code", code)
            append("redirect_uri", slackRedirectUri())
        }
    },
)
    .text()
    .await()
    .let { JSON.parse(it) }
