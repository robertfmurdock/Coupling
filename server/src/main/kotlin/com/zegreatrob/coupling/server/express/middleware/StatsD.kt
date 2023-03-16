package com.zegreatrob.coupling.server.express.middleware

import com.zegreatrob.coupling.server.external.statsd.statsd
import kotlin.js.json

fun statsD() = statsd(
    json(
        "host" to "statsd",
        "port" to 8125,
    ),
)
