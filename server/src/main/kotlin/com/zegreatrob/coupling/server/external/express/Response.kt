package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Response {
    var statusCode: Int

    operator fun get(key: String): Any?

    fun send(body: Json?)
    fun send(body: Array<*>)
    fun send(body: Any?)

    fun sendStatus(statusCode: Int)
}

fun Response.sendSuccessful(body: Any?) {
    this.statusCode = 200
    send(body)
}