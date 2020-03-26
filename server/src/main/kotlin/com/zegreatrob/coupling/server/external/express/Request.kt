package com.zegreatrob.coupling.server.external.express

import com.benasher44.uuid.Uuid
import kotlin.js.Json

external interface Request {
    val params: Json

    val body: dynamic
    val method: String
    val originalUrl: String?
    val url: String
    val commandDispatcher: dynamic
    val user: dynamic
    var traceId: Uuid?
    fun logout()
}

fun Request.jsonBody() = body.unsafeCast<Json>()
fun Request.jsonArrayBody() = body.unsafeCast<Array<Json>>()