package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> T.buildCommandFunc(scope: CoroutineScope): CommandFunc<T> = { runCommands ->
    { scope.launch { runCommands() } }
}

fun <C : SuspendAction<D, R>, D, R> CommandFunc<D>.makeItSo(
    buildCommand: () -> C,
    response: (Result<R>) -> Unit
) = this {
    buildCommand()
        .execute(this)
        .let(response)
}
