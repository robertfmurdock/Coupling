package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.graphql.graphqlHTTP
import com.zegreatrob.coupling.server.graphql.unifiedSchema
import com.zegreatrob.coupling.server.slack.slackInstallProvider
import com.zegreatrob.coupling.server.slack.slackRedirectUri
import js.core.jso
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    get("/api/health", healthRoute())
    use(userLoadingMiddleware())
    get("/api/integration/slack-install-url") { _, response, _ ->
        slackInstallProvider.generateInstallUrl(
            jso {
                scopes = arrayOf("chat:write", "chat:write.customize", "channels:history", "commands")
                redirectUri = slackRedirectUri()
            },
        ).then(response::send)
    }
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to unifiedSchema())))
    get("*", indexRoute())
}
