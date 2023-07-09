package com.zegreatrob.coupling.server.express

import com.zegreatrob.coupling.server.external.Done
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun <T> CoroutineScope.async(done: Done, block: suspend () -> T) {
    launch { done(null, block()) }
        .invokeOnCompletion { cause -> if (cause != null) done(cause, null) }
}
