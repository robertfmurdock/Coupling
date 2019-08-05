package com.zegreatrob.coupling.client.external.axios

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("axios")
@JsNonModule
external val axios: Axios

external interface Axios {
    fun post(url: String, options: Json): Promise<Unit>
    fun get(url: String): Promise<dynamic>
}

fun Axios.getList(url: String): Promise<Array<Json>> = get(url)
        .then<dynamic> { result -> result.data.unsafeCast<Array<Json>>() }
