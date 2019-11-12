package com.zegreatrob.coupling.server.external.express

external interface Response {
    val statusCode: Int

    operator fun get(key: String): Any?
}