package com.zegreatrob.coupling.server.express

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.server.external.express.Handler

fun tracer(): Handler = { request, _, next -> request.asDynamic().traceId =
    uuid4(); next() }