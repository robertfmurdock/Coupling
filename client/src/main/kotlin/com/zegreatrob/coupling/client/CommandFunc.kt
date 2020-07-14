package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

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

    @ExperimentalCoroutinesApi
    override fun <C : SuspendResultAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit) =
        fun() {
            launchExecute(dispatcherFunc(), commandFunc(), response)
        }

    @ExperimentalCoroutinesApi
    private fun <C : SuspendResultAction<D, R>, R> launchExecute(
        dispatcher: D,
        command: C,
        response: (Result<R>) -> Unit
    ) {
        val deferred = scope.async { dispatcher.execute(command) }
        deferred.invokeOnCompletion { handler ->
            if (handler != null) {
                console.error("Failed to finish ${command::class.simpleName} because ${handler.message}")
            } else {
                deferred.getCompleted().let(response)
            }
        }
    }

}
