package com.zegreatrob.coupling.e2e.test

import kotlin.js.Json

object Process {
    private val env: Json get() = js("process.env").unsafeCast<Json>()

    fun getEnv(key: String): String? = env[key].unsafeCast<String?>()
    fun send(@Suppress("UNUSED_PARAMETER") message: String) {
        js("if(process.send){ process.send(message) }")
    }
}
