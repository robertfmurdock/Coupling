package com.zegreatrob.coupling.client.external.react

import com.zegreatrob.coupling.action.ScopeProvider
import kotlinx.coroutines.*
import react.RProps

interface ComponentBuilder<P : RProps> {
    fun build(): ReactFunctionComponent<P>
}

fun useScope(coroutineName: String): CoroutineScope {
    val (scope) = useState { MainScope() + CoroutineName(coroutineName) }
    useEffectWithCleanup(arrayOf()) {
        { scope.cancel() }
    }
    return scope
}
