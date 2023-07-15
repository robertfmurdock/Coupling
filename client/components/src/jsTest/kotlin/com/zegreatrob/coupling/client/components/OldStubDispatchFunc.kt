package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.async.SuspendAction

class DispatchedFunc<C, R>(val command: C, val responseFunc: (R) -> Unit)

class OldStubDispatchFunc<D>(private val stubber: OldStubDispatcher = OldStubDispatcher()) : DispatchFunc<D> {

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

    override fun invoke(block: suspend ActionCannon<D>.() -> Unit): () -> Unit {
        TODO("Will not")
    }
}
