package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Request {
    val params: Json
    val body: dynamic
    val method: String
    val originalUrl: String?
    val url: String
}

fun Request.jsonBody() = body.unsafeCast<Json>()
fun Request.jsonArrayBody() = body.unsafeCast<Array<Json>>()