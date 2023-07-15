package com.zegreatrob.coupling.client.components

import com.zegreatrob.testmints.action.ActionWrapper

class OldStubDispatcher {
    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    fun <D> func() = OldStubDispatchFunc<D>(this)

    fun <C, R> sendResult(result: R) {
        dispatchList.filterIsInstance<DispatchedFunc<ActionWrapper<C>, R>>()
            .firstOrNull()
            ?.responseFunc
            ?.invoke(result)
    }

    inline fun <reified C> commandsDispatched() = dispatchList.map(DispatchedFunc<*, *>::command).filterIsInstance<C>()
}
