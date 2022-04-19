@file:JsModule("jwks-rsa")
package com.zegreatrob.coupling.server.external.jwksrsa

import kotlin.js.Json

external fun expressJwtSecret(json: Json): Any
