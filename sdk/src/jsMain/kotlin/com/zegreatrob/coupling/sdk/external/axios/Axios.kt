package com.zegreatrob.coupling.sdk.external.axios

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asDeferred
import kotlin.js.Json
import kotlin.js.Promise

@JsModule("axios")
external val axios: Axios

external interface Axios {
    fun post(url: String, body: dynamic): Promise<dynamic>
    fun get(url: String, config: dynamic = definedExternally): Promise<Result>
    fun delete(url: String): Promise<dynamic>

    fun create(config: dynamic): Axios

    val default: Axios
    val defaults: dynamic
}



external interface Result {
    val status: Int
    val data: dynamic
}

fun Axios.getList(url: String): Promise<Array<Json>> = get(url)
    .then<dynamic> { result -> result.data.unsafeCast<Array<Json>>() }
