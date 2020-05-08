package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> T.buildCommandFunc(scope: CoroutineScope): CommandFunc<T> = { runCommands ->
    { scope.launch { runCommands() } }
}

interface CommandFunc2<D1> {
    val commandFunc: CommandFunc<D1>
}

class DispatchFunc<D1>(override val commandFunc: CommandFunc<D1>) : CommandFunc2<D1>

operator fun <C : SuspendAction<D2, R>, D1 : D2, D2, R> CommandFunc2<D1>.invoke(
    buildCommand: () -> C,
    response: (Result<R>) -> Unit
) = commandFunc {
    buildCommand()
        .execute(this)
        .let(response)
}
