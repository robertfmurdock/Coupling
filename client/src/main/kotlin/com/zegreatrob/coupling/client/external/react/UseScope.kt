package com.zegreatrob.coupling.client.external.react

import kotlinx.coroutines.*
import react.RProps

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffectWithCleanup(arrayOf()) {
        { scope.cancel() }
    }
    return scope
}
