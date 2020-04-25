package com.zegreatrob.coupling.server.external.express_session

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

@JsModule("express-session")
@JsNonModule
external fun session(config: Json): Handler

@JsModule("express-session")
@JsNonModule
external val expressSession: dynamic