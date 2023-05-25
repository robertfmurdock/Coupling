package com.zegreatrob.coupling.action

actual inline fun <reified T> loadResource(fileResource: String): T {
    @Suppress("UNUSED_VARIABLE")
    val path = "./$fileResource"
    return js("require(path)").unsafeCast<T>()
}
