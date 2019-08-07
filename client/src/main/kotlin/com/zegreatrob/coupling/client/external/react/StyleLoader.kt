package com.zegreatrob.coupling.client

fun <T> loadStyles(@Suppress("UNUSED_PARAMETER") name: String): T {
    return js("require('com/zegreatrob/coupling/client/'+ name +'.css')").unsafeCast<T>()
}
