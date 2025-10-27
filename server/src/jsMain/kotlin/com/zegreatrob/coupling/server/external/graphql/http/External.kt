@file:JsModule("graphql-http/lib/use/express")

package com.zegreatrob.coupling.server.external.graphql.http

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.graphql.GraphQLSchema
import kotlinx.js.JsPlainObject

external fun createHandler(options: GqlHttpExpressOptions): Handler

@JsPlainObject
external interface GqlHttpExpressOptions {
    val schema: GraphQLSchema
    val context: (Request) -> dynamic
}

external interface Request {
    val raw: com.zegreatrob.coupling.server.external.express.Request
}
