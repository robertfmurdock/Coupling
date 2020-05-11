package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
            decoratedExecute(dispatcherProvider(), buildCommand(), execute)
                .let(response)
        }
    }

    suspend fun <C : Action, R> decoratedExecute(
        dispatcher: D,
        command: C,
        execute: suspend C.(D) -> Result<R>
    ) = with(dispatcher) { command.logAsync { execute(command, dispatcher) } }

}

operator fun <C : SuspendAction<D2, R>, D1 : D2, D2, R> DispatchFunc<D1>.invoke(
    buildCommand: () -> C,
    response: (Result<R>) -> Unit
) = makeItSo(response, buildCommand, SuspendAction<D2, R>::execute)
