package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias CommandFunc<T> = (suspend T.() -> Unit) -> () -> Unit

fun <T> T.buildCommandFunc(scope: CoroutineScope): CommandFunc<T> = { runCommands ->
    { scope.launch { runCommands() } }
}

interface DispatchFunc<D> {
    fun <C : Action, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ): () -> Unit
}

class DecoratedDispatchFunc<D : ActionLoggingSyntax>(
    val dispatcherProvider: () -> D,
    private val scope: CoroutineScope
) : DispatchFunc<D> {

    override fun <C : Action, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ): () -> Unit = {
        scope.launch {
            with(dispatcherProvider()) {
                buildCommand().let { command ->
                    command.logAsync {
                        execute(command, this).let(response)
                    }
                }
            }
        }
    }

}

operator fun <C : SuspendAction<D2, R>, D1 : D2, D2, R> DispatchFunc<D1>.invoke(
    buildCommand: () -> C,
    response: (Result<R>) -> Unit
) = makeItSo(response, buildCommand, SuspendAction<D2, R>::execute)
