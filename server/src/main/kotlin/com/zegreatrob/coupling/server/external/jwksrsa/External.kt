@file:JsModule("jwks-rsa")

package com.zegreatrob.coupling.server.external.jwksrsa

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

external fun expressJwtSecret(json: Json): (request: Request, token: dynamic) -> dynamic
