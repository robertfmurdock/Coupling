package com.zegreatrob.coupling.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> buildCommandFunc(scope: CoroutineScope, dispatcher: T): CommandFunc<T> = { runCommands ->
    { scope.launch { dispatcher.runCommands() } }
}
