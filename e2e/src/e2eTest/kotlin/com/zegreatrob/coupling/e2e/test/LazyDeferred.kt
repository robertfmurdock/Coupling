package com.zegreatrob.coupling.e2e.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T) = lazy {
    MainScope().async(start = CoroutineStart.LAZY) {
        block.invoke(this)
    }
}
