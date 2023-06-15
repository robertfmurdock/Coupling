package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.graphql.graphqlHTTP
import com.zegreatrob.coupling.server.graphql.unifiedSchema
import com.zegreatrob.coupling.server.slack.exchangeCodeForAccessToken
import com.zegreatrob.coupling.server.slack.slackInstallProvider
import com.zegreatrob.coupling.server.slack.slackRedirectUri
import js.core.jso
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    get("/api/health", healthRoute())
    use(userLoadingMiddleware())
    get("/api/integration/slack") { request, response, _ ->
        val code = "${request.query["code"]}"
        MainScope().launch {
            val result = exchangeCodeForAccessToken(code)
            response.send(JSON.stringify(result))
        }.invokeOnCompletion { it?.printStackTrace() }
    }
    get("/api/integration/slack-install-url") { _, response, _ ->
        slackInstallProvider.generateInstallUrl(
            jso {
                scopes = arrayOf("chat:write", "chat:write.customize", "commands")
                redirectUri = slackRedirectUri()
            },
        ).then(response::send)
    }
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to unifiedSchema())))
    get("*", indexRoute())
}
