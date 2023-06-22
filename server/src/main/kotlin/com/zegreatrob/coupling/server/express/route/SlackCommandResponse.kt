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

    val targetParams = URLSearchParams(emptyArray()).apply {
        append("slackTeam", params["team_id"] ?: "")
        append("slackChannel", params["channel_id"] ?: "")
    }

    response.send(
        json(
            "blocks" to arrayOf(
                json(
                    "type" to "header",
                    "text" to json(
                        "type" to "plain_text",
                        "text" to "Install Coupling",
                        "emoji" to true,
                    ),
                ),
                json(
                    "type" to "section",
                    "fields" to arrayOf(
                        json(
                            "type" to "mrkdwn",
                            "text" to "Click [here](${Config.publicUrl}/integration/slack/connect?$targetParams)!",
                        ),
                    ),
                ),
            ),
        ),
    )
}
