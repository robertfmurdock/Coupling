package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Express
import com.zegreatrob.coupling.server.external.express_graphql.graphqlHTTP
import com.zegreatrob.coupling.server.graphql.couplingSchema
import kotlin.js.json

fun Express.routes() {
    get("/", indexRoute())
    use(userLoadingMiddleware())
    all("/api/*", apiGuard())
    use("/api/graphql", graphqlHTTP(json("schema" to couplingSchema(), "graphiql" to true)))
    get("*", indexRoute())
}
