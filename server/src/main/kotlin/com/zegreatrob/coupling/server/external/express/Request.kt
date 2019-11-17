package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Request {
    val params: Json
    val body: dynamic
    val method: String
    val originalUrl: String?
    val url: String
    val commandDispatcher: dynamic
    val user: dynamic
}

fun Request.jsonBody() = body.unsafeCast<Json>()
fun Request.jsonArrayBody() = body.unsafeCast<Array<Json>>()