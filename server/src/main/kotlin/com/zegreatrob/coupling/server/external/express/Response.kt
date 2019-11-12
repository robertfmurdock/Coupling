package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Response {
    var statusCode: Int

    operator fun get(key: String): Any?

    fun send(body: Json?)
}