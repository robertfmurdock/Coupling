package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.components.DispatchFunc
import com.zegreatrob.testmints.action.async.SuspendAction

class StubDispatcher {
    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    fun <D> func() = StubDispatchFunc<D>(this)

    private fun <C, R> commandFunctionsDispatched() = dispatchList.filterIsInstance<DispatchedFunc<C, R>>()

    inline fun <reified C> commandsDispatched() = dispatchList.map { it.command }.filterIsInstance<C>()

    fun <C> simulateSuccess() = commandFunctionsDispatched<C, Unit>().map {
        it.responseFunc(Unit)
    }
}

class DispatchedFunc<C, R>(val command: C, val responseFunc: (R) -> Unit)

class StubDispatchFunc<D>(private val stubber: StubDispatcher = StubDispatcher()) : DispatchFunc<D> {

    override fun <C : SuspendAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit
    ): () -> Unit = {
        stubber.dispatchList.add(DispatchedFunc(commandFunc(), response))
    }
}
