package com.zegreatrob.coupling.server.external.passport

import com.zegreatrob.coupling.server.external.express.Handler
import kotlin.js.Json
import kotlin.reflect.KFunction2

@JsModule("passport")
@JsNonModule
external val passport: Passport

external interface Passport {
    fun authenticate(strategy: String): Handler
    fun authenticate(strategy: String, options: Json): Handler
    fun initialize(): Handler
    fun session(): Handler
    fun serializeUser(kFunction2: KFunction2<dynamic, (dynamic, dynamic) -> Unit, Unit>)
    fun deserializeUser(kFunction2: KFunction2<String, (dynamic, dynamic) -> Unit, Unit>)
    fun use(googleAuthTransferStrategy: dynamic)
}
