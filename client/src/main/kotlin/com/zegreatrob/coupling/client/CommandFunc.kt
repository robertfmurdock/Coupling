package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface DispatchFunc<D> {
    operator fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit
}

class DecoratedDispatchFunc<D : ActionLoggingSyntax>(
    val dispatcherProvider: () -> D,
    private val scope: CoroutineScope
) : DispatchFunc<D> {

    override fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit =
        {
            scope.launch {
                decoratedExecute(dispatcherProvider(), commandFunc(), SuspendAction<D, R>::execute)
                    .let(response)
            }
        }

    suspend fun <C : Action, R> decoratedExecute(
        dispatcher: D,
        command: C,
        execute: suspend C.(D) -> Result<R>
    ) = with(dispatcher) { command.logAsync { execute(command, dispatcher) } }

}

