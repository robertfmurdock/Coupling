@file:JsModule("passport-azure-ad")
@file:JsNonModule
package com.zegreatrob.coupling.server.external.passportazuread

import kotlin.js.Json

external class OIDCStrategy(
    config: Json,
    function: (String, String, Json, String, String, (dynamic, dynamic) -> Unit) -> Unit
)