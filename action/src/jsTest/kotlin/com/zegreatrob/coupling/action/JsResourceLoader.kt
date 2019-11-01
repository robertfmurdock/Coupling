package com.zegreatrob.coupling.action

actual inline fun <reified T> loadResource(@Suppress("UNUSED_PARAMETER") fileResource: String): T {
    return js("require(fileResource)").unsafeCast<T>()
}