package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.slack.slackRequestVerifier
import node.buffer.Buffer
import node.buffer.BufferEncoding

fun verifySlackSignature(): Handler = { request, response, next ->
    val timestamp = request.get("X-Slack-Request-Timestamp")?.toIntOrNull()
    val signature = request.get("X-Slack-Signature")
    val body = request.body as? Buffer
    if (timestamp == null || body == null) {
        response.sendStatus(400)
    } else {
        val bodyString = body.toString(encoding = BufferEncoding.utf8)
        val expectedSignature = slackRequestVerifier.signature(timestamp, bodyString)
        if (expectedSignature != signature) {
            response.sendStatus(400)
        } else {
            next()
        }
    }
}
