package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.ErrorResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax
import com.zegreatrob.testmints.action.async.execute
import kotlinx.coroutines.ExperimentalCoroutinesApi

interface DispatchFunc<D> {
    operator fun <C : SuspendResultAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (Result<R>) -> Unit
    ): () -> Unit
}

class DecoratedDispatchFunc<D : SuspendActionExecuteSyntax>(
    val dispatcherFunc: () -> D,
    private val bridge: DataLoadComponentTools
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
        onResponse: (Result<R>) -> Unit
    ) = bridge.performSuspendWork(
        { dispatcher.execute(command) },
        { handler: Throwable -> makeErrorResult(command, handler) },
        onResponse
    )

    private fun <C : SuspendResultAction<D, R>, R> makeErrorResult(command: C, handler: Throwable) = ErrorResult<R>(
        "Failed to finish ${command::class.simpleName} because ${handler.message}"
    ).also { console.error(it.message) }

}
