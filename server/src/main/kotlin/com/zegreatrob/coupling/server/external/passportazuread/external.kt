@file:JsModule("passport-azure-ad")


package com.zegreatrob.coupling.server.external.passportazuread

import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

external class OIDCStrategy(
    config: Json,
    function: (Request, String, String, Json, String, String, (dynamic, dynamic) -> Unit) -> Unit
)