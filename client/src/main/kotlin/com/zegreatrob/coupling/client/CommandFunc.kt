package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> T.buildCommandFunc(scope: CoroutineScope): CommandFunc<T> = { runCommands ->
    { scope.launch { runCommands() } }
}

interface CommandFunc2<D> {
    fun <C, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ): () -> Unit
}

class DispatchFunc<D>(val commandFunc: CommandFunc<D>) : CommandFunc2<D> {
    override fun <C, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ) = commandFunc {
        execute(buildCommand(), this).let(response)
    }
}

operator fun <C : SuspendAction<D2, R>, D1 : D2, D2, R> CommandFunc2<D1>.invoke(
    buildCommand: () -> C,
    response: (Result<R>) -> Unit
) = makeItSo(response, buildCommand, SuspendAction<D2, R>::execute)
