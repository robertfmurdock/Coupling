package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.ActionWrapper
import com.zegreatrob.testmints.action.async.SuspendAction

class StubDispatcher {
    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    fun <D> func() = StubDispatchFunc<D>(this)

    fun <C, R> sendResult(result: R) where C : SuspendAction<*, R> {
        commandFunctionsDispatched<C, _, _>()
            .firstOrNull()
            ?.responseFunc
            ?.invoke(result)
    }

    fun <C, R> wrappedSendResult(result: R) {
        dispatchList.filterIsInstance<DispatchedFunc<ActionWrapper<C>, R>>()
            .firstOrNull()
            ?.responseFunc
            ?.invoke(result)
    }

    fun <C, D, R> commandFunctionsDispatched() where C : SuspendAction<D, R> =
        dispatchList.filterIsInstance<DispatchedFunc<C, R>>()

    inline fun <reified C> commandsDispatched() = dispatchList.map { it.command }.filterIsInstance<C>()

    fun <C, D> sendResult() where C : SuspendAction<D, Unit> = commandFunctionsDispatched<C, _, Unit>().map {
        it.responseFunc(Unit)
    }
}

class DispatchedFunc<C, R>(val command: C, val responseFunc: (R) -> Unit)

class StubDispatchFunc<D>(private val stubber: StubDispatcher = StubDispatcher()) : DispatchFunc<D> {

    override fun <C : SuspendAction<D, R>, R> invoke(
        commandFunc: () -> C,
        response: (R) -> Unit,
    ): () -> Unit = {
        stubber.dispatchList.add(DispatchedFunc(commandFunc(), response))
    }

    override fun <C, R> invoke(
        commandFunc: () -> C,
        fireFunc: suspend ActionCannon<D>.(C) -> R,
        response: (R) -> Unit,
    ): () -> Unit = {
        stubber.dispatchList.add(DispatchedFunc(commandFunc(), response))
    }
}
