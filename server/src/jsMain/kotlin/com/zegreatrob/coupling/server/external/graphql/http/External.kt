
@file:JsModule("graphql-http/lib/use/express")

package com.zegreatrob.coupling.server.external.graphql.http

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema

external fun createHandler(options: GraphQlHttpExpressOptions): Handler

external interface GraphQlHttpExpressOptions {
    var schema: GraphQLSchema
    var context: (Request) -> dynamic
}

external interface Request {
    val raw: com.zegreatrob.coupling.server.external.express.Request
}
