package com.zegreatrob.coupling.server.external.serverless.http

import com.zegreatrob.coupling.server.external.express.Express

@JsModule("serverless-http")
external fun serverlessHttp(
    express: Express,
    context: dynamic = definedExternally,
): (event: dynamic, context: dynamic) -> dynamic
