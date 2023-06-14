package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express.graphql.graphqlHTTP
import com.zegreatrob.coupling.server.graphql.unifiedSchema
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    get("/api/health", healthRoute())
    get("/integration/slack") { request, response, _ ->
        val code = request.query["code"]
        response.send("Got code $code")
    }
    use(userLoadingMiddleware())
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to unifiedSchema())))
    get("*", indexRoute())
}
