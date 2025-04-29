package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.raw
import com.zegreatrob.coupling.server.external.express.urlencoded
import com.zegreatrob.coupling.server.external.graphql.http.createHandler
import com.zegreatrob.coupling.server.graphql.unifiedSchema
import js.objects.unsafeJso
import kotlinx.coroutines.CoroutineScope
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    get("/latest-cli", cliRoute())
    get("/api/health", healthRoute())
    use(userLoadingMiddleware())
    post(
        "/api/integration/slack/command",
        raw(json("type" to "*/*")),
        verifySlackSignature(),
        slackCommandResponse(),
    )
    all("/api/*", apiGuard())
    use("/api/graphql", urlencoded(json("extended" to true)), com.zegreatrob.coupling.server.external.express.json())
    use(
        "/api/graphql",
        createHandler(
            unsafeJso {
                schema = unifiedSchema()
                context = { request -> CouplingContext(request.raw.scope, request.raw.commandDispatcher) }
            },
        ),
    )
    get("*", indexRoute())
}

data class CouplingContext(val scope: CoroutineScope, val commandDispatcher: CommandDispatcher)
