package com.zegreatrob.coupling.server.e2e

object Config {
    val port: Int = process.env.PORT.unsafeCast<Int?>() ?: 3000
    val publicUrl: String = process.env.PUBLIC_URL.unsafeCast<String?>() ?: "http://localhost:${port}"
}