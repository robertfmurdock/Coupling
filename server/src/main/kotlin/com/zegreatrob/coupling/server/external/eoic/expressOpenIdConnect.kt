@file:JsModule("express-openid-connect")

package com.zegreatrob.coupling.server.external.eoic

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json

@JsName("auth")
external fun expressOpenIdConnect(config: Json = definedExternally): Handler
