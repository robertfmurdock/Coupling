package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.graphql.graphqlHTTP
import com.zegreatrob.coupling.server.external.express.raw
import com.zegreatrob.coupling.server.graphql.unifiedSchema
import com.zegreatrob.coupling.server.slack.slackInstallProvider
import com.zegreatrob.coupling.server.slack.slackRedirectUri
import com.zegreatrob.coupling.server.slack.slackRequestVerifier
import js.core.jso
import node.buffer.Buffer
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    get("/api/health", healthRoute())
    use(userLoadingMiddleware())
    get("/api/integration/slack-install-url") { _, response, _ ->
        slackInstallProvider.generateInstallUrl(
            jso {
                scopes = arrayOf("chat:write", "chat:write.customize", "channels:history", "groups:history", "commands")
                redirectUri = slackRedirectUri()
            },
        ).then(response::send)
    }
    post("/api/integration/slack/command", raw())
    post("/api/integration/slack/command") { request, response, _ ->
        val timestamp = request.get("X-Slack-Request-Timestamp")?.toIntOrNull()
        val signature = request.get("X-Slack-Signature")
        val body = request.body as? Buffer
        if (timestamp != null && body != null) {
            val expectedSignature = slackRequestVerifier.signature(timestamp, body.toString())
            if (expectedSignature != signature) {
                response.send("bad signature")
                response.sendStatus(400)
            } else {
                response.send(200)
            }
        } else {
            response.sendStatus(400)
        }
    }
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to unifiedSchema())))
    get("*", indexRoute())
}
