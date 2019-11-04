package com.zegreatrob.coupling.client.sdk.axios.axios

import kotlin.js.Json
import kotlin.js.Promise

@JsModule("axios")
external val axios: Axios

external interface Axios {
    fun post(url: String, body: dynamic): Promise<dynamic>
    fun get(url: String): Promise<dynamic>
    fun delete(url: String): Promise<dynamic>
}

fun Axios.getList(url: String): Promise<Array<Json>> = get(url)
    .then<dynamic> { result -> result.data.unsafeCast<Array<Json>>() }
