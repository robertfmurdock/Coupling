package com.zegreatrob.coupling.server

import kotlin.js.Json

object Process {
    val env: Json get() = js("process.env")

    fun getEnv(key: String): String? = env[key].unsafeCast<String?>()
}