package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.actionFunc.Result
import com.zegreatrob.coupling.actionFunc.SuccessfulResult
import com.zegreatrob.coupling.actionFunc.SuspendResultAction

class StubDispatchFunc<D> : DispatchFunc<D> {

    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    override fun <C : SuspendResultAction<D, R>, R> invoke(commandFunc: () -> C, response: (Result<R>) -> Unit): () -> Unit =
        { dispatchList.add(DispatchedFunc(commandFunc(), response)) }

    class DispatchedFunc<C, R>(val command: C, val responseFunc: (Result<R>) -> Unit)

    private fun <C, R> commandFunctionsDispatched() = dispatchList.filterIsInstance<DispatchedFunc<C, R>>()

    inline fun <reified C> commandsDispatched() = dispatchList.map { it.command }.filterIsInstance<C>()

    fun <C> simulateSuccess() = commandFunctionsDispatched<C, Unit>().map {
        it.responseFunc(SuccessfulResult(Unit))
    }

}
