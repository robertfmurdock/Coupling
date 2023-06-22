package com.zegreatrob.coupling.server.express.middleware

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.server.external.express.Handler

fun tracer(): Handler = { request, _, next ->
    request.asDynamic().traceId = request.get("X-Request-Id") ?: uuid4()
    next()
}
