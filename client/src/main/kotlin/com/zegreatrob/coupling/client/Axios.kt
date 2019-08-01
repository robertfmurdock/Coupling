package com.zegreatrob.coupling.client

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("axios")
@JsNonModule
external val axios: Axios

external interface Axios {
    fun post(url: String, options: Json): Promise<Unit>
}

