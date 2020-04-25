package com.zegreatrob.coupling.server.external.passport

import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.server.external.Done
import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Request
import kotlin.js.Json

@JsModule("passport")
@JsNonModule
external val passport: Passport

external interface Passport {
    fun authenticate(strategy: String): Handler
    fun authenticate(strategy: String, options: Json): Handler
    fun initialize(): Handler
    fun session(): Handler
    fun serializeUser(kFunction2: (User, Done) -> Unit)
    fun deserializeUser(kFunction2: (Request, String, Done) -> Unit)
    fun use(googleAuthTransferStrategy: dynamic)
}
