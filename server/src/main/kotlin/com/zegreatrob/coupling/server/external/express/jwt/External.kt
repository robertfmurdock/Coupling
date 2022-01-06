package com.zegreatrob.coupling.server.external.express.jwt

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

@JsModule("express-jwt")
external fun jwt(jwtConfig: Json): Handler
