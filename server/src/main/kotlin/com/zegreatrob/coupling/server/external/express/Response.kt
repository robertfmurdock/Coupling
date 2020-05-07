package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Response {
    var statusCode: Int

    operator fun get(key: String): Any?

    fun send(body: Json?)
    fun send(body: Array<*>)
    fun send(body: Any?)

    fun sendStatus(statusCode: Int)
    fun redirect(path: String)
    fun render(view: String, json: Json)
}
