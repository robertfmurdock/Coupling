package com.zegreatrob.coupling.server.external.passport

@JsModule("passport")
@JsNonModule
external val passport: Passport

external class Passport {
    fun initialize(): dynamic
    fun session(): dynamic
}
