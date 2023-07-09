@file:JsModule("express-jwt")

package com.zegreatrob.coupling.server.external.express.jwt

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

external fun expressjwt(jwtConfig: Json): Handler
