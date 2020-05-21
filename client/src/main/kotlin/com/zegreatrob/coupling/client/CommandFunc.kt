package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.LoggingCommandExecuteSyntax
import com.zegreatrob.coupling.actionFunc.Result
import com.zegreatrob.coupling.actionFunc.SuspendAction
import com.zegreatrob.coupling.actionFunc.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface DispatchFunc<D> {
    operator fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit
}

class DecoratedDispatchFunc<D : LoggingCommandExecuteSyntax>(
    val dispatcherFunc: () -> D,
    private val scope: CoroutineScope
) : DispatchFunc<D> {

    override fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit =
        {
            launchExecute(dispatcherFunc(), commandFunc(), response)
        }

    private fun <C : SuspendAction<D, R>, R> launchExecute(dispatcher: D, command: C, response: (Result<R>) -> Unit) =
        scope.launch {
            dispatcher.execute(command).let(response)
        }

}
