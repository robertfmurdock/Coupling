package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface DispatchFunc<D> {
    operator fun <C : SuspendResultAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (Result<R>) -> Unit
    ): () -> Unit
}

class DecoratedDispatchFunc<D : SuspendActionExecuteSyntax>(
    val dispatcherFunc: () -> D,
    private val scope: CoroutineScope
) : DispatchFunc<D> {

    override fun <C : SuspendResultAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit) =
        fun() {
            launchExecute(dispatcherFunc(), commandFunc(), response)
        }

    private fun <C : SuspendResultAction<D, R>, R> launchExecute(
        dispatcher: D,
        command: C,
        response: (Result<R>) -> Unit
    ) = scope.launch {
        dispatcher.execute(command)
            .let(response)
    }

}
