package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Handler
import node.buffer.Buffer
import node.buffer.BufferEncoding
import node.url.URLSearchParams
import kotlin.js.Json
import kotlin.js.json

fun slackCommandResponse(): Handler = { request, response, _ ->
    val body = request.body as Buffer
    val params = URLSearchParams(body.toString(encoding = BufferEncoding.utf8))

    val slackTeam = params["team_id"] ?: ""
    val slackChannel = params["channel_id"] ?: ""
    val text = params["text"] ?: ""

    response.send(
        when (text) {
            "connect" -> connectMessage(slackTeam, slackChannel)
            "help" -> helpMessage()
            else -> helpMessage()
        },
    )
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
            "text" to json(
                "type" to "mrkdwn",
                "text" to "First, invite the coupling bot to your preferred channel. Simply type `/invite @coupling`.",
            ),
        ),
        json(
            "type" to "section",
            "text" to json(
                "type" to "mrkdwn",
                "text" to "Then, click the button on the right to connect your Coupling party.",
            ),
            "accessory" to json(
                "type" to "button",
                "text" to json(
                    "type" to "plain_text",
                    "text" to "Click here!",
                    "emoji" to true,
                ),
                "value" to "connect_link",
                "url" to connectUrl(slackTeam, slackChannel),
            ),
        ),
    ),
)

private fun helpMessage(): Json {
    val connectDescription = """
        *connect*
        This connects the current Slack channel to a Coupling party, so that when a new spin occurs a message is sent to the channel.

            /coupling connect
    """.trimIndent()
    val helpDescription = """
        *help*
        This tells you what you can do with the Coupling Slack app.

            /coupling help
    """.trimIndent()
    return json(
        "blocks" to arrayOf(
            json(
                "type" to "header",
                "text" to json(
                    "type" to "plain_text",
                    "text" to "Coupling Slack Help",
                    "emoji" to true,
                ),
            ),
            json(
                "type" to "section",
                "text" to json(
                    "type" to "mrkdwn",
                    "text" to "There are a few things you can do with Coupling through slack.",
                ),
            ),
            json(
                "type" to "section",
                "text" to json(
                    "type" to "mrkdwn",
                    "text" to connectDescription,
                ),
            ),
            json(
                "type" to "section",
                "text" to json(
                    "type" to "mrkdwn",
                    "text" to helpDescription,
                ),
            ),
        ),
    )
}

private fun connectUrl(slackTeam: String, slackChannel: String): String {
    val targetParams = URLSearchParams(emptyArray()).apply {
        append("slackTeam", slackTeam)
        append("slackChannel", slackChannel)
    }

    return "${Config.publicUrl}/integration/slack/connect?$targetParams"
}
