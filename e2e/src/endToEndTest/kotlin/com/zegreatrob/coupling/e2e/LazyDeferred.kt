package com.zegreatrob.coupling.e2e

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T) = lazy {
    GlobalScope.async(start = CoroutineStart.LAZY) {
        block.invoke(this)
    }
}
