package com.zegreatrob.coupling.server.external.express

import kotlin.js.Json

external interface Request {
    val params: Json
    val body: Json
    val method: String
    val originalUrl: String?
    val url: String
}