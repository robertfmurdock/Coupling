package com.zegreatrob.coupling.server.external.express

import com.benasher44.uuid.Uuid
import com.zegreatrob.coupling.server.CommandDispatcher
import kotlin.js.Json

external interface Request {
    val params: Json
    val body: dynamic
    val method: String
    val path: String
    val originalUrl: String?
    val url: String
    fun logout()
    fun isAuthenticated(): Boolean
    fun close()

    var commandDispatcher: CommandDispatcher
    val user: Json
    var traceId: Uuid?
    var statsdkey: String?
}

fun Request.jsonBody() = body.unsafeCast<Json>()
fun Request.jsonArrayBody() = body.unsafeCast<Array<Json>>()