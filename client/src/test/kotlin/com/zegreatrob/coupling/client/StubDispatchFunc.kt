package com.zegreatrob.coupling.client

import com.zegreatrob.testmints.action.async.SuspendAction

class StubDispatchFunc<D> : DispatchFunc<D> {

    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    override fun <C : SuspendAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit
    ): () -> Unit = {
        dispatchList.add(DispatchedFunc(commandFunc(), response))
    }

    class DispatchedFunc<C, R>(val command: C, val responseFunc: (R) -> Unit)

    private fun <C, R> commandFunctionsDispatched() = dispatchList.filterIsInstance<DispatchedFunc<C, R>>()

    inline fun <reified C> commandsDispatched() = dispatchList.map { it.command }.filterIsInstance<C>()

    fun <C> simulateSuccess() = commandFunctionsDispatched<C, Unit>().map {
        it.responseFunc(Unit)
    }
}
