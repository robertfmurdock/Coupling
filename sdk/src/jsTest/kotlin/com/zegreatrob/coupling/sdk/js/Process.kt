package com.zegreatrob.coupling.sdk.js

import kotlin.js.Json

object Process {
    private val env: Json get() = js("process.env").unsafeCast<Json>()
    fun getEnv(key: String): String? = env[key].unsafeCast<String?>()
}
