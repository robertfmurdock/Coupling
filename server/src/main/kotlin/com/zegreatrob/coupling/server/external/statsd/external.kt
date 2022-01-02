package com.zegreatrob.coupling.server.external.statsd

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

@JsModule("express-statsd")
@JsNonModule
external fun statsd(config: Json): Handler
