package com.zegreatrob.coupling.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> T.buildCommandFunc(scope: CoroutineScope): CommandFunc<T> = { runCommands ->
    { scope.launch { runCommands() } }
}
