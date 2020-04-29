package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.testmints.async.ScopeMint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

fun <T> lazyDeferred(block: suspend CoroutineScope.() -> T) = lazy {
    GlobalScope.async(start = CoroutineStart.LAZY) {
        block.invoke(this)
    }
}

fun <T> ScopeMint.setupLazyDeferred(block: suspend CoroutineScope.() -> T) = lazy {
    setupScope.async(start = CoroutineStart.LAZY) {
        block.invoke(this)
    }
}