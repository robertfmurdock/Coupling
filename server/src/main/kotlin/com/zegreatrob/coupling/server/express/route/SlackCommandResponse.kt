package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler
import node.buffer.Buffer
import node.buffer.BufferEncoding
import node.url.URLSearchParams
import kotlin.js.json

fun slackCommandResponse(): Handler = { request, response, _ ->
    val body = request.body as Buffer
    val params = URLSearchParams(body.toString(encoding = BufferEncoding.utf8))

    val slackTeam = params["team_id"] ?: ""
    val slackChannel = params["channel_id"] ?: ""

    response.send(connectMessage(slackTeam, slackChannel))
}

private fun connectMessage(slackTeam: String, slackChannel: String) = json(
    "blocks" to arrayOf(
        json(
            "type" to "header",
            "text" to json(
                "type" to "plain_text",
                "text" to "Connect Channel to Coupling",
                "emoji" to true,
            ),
        ),
        json(
            "type" to "section",
            "fields" to arrayOf(
                json(
                    "type" to "button",
                    "text" to "Click here!",
                    "value" to "connect_link",
                    "url" to connectUrl(slackTeam, slackChannel),
                ),
            ),
        ),
    ),
)

private fun connectUrl(slackTeam: String, slackChannel: String): String {
    val targetParams = URLSearchParams(emptyArray()).apply {
        append("slackTeam", slackTeam)
        append("slackChannel", slackChannel)
    }

    return "${Config.publicUrl}/integration/slack/connect?$targetParams"
}
