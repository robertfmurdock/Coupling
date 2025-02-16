package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.uuid.Uuid

fun tracer(): Handler = { request, _, next ->
    request.asDynamic().traceId = request.get("X-Request-Id") ?: Uuid.random()
    next()
}
