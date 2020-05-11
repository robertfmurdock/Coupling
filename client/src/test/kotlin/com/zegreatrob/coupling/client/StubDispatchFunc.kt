package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SuccessfulResult

class StubDispatchFunc<D> : DispatchFunc<D> {

    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    override fun <C : Action, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ): () -> Unit = { dispatchList.add(DispatchedFunc(buildCommand(), response)) }

    data class DispatchedFunc<C, R>(val command: C, val responseFunc: (Result<R>) -> Unit)


    fun <C, R> commandFuncsDispatched() = dispatchList.filterIsInstance<DispatchedFunc<C, R>>()
    
    inline fun <reified C> commandsDispatched() = dispatchList.map { it.command }.filterIsInstance<C>()

    fun <C> simulateSuccess() = commandFuncsDispatched<C, Unit>().map {
        it.responseFunc(SuccessfulResult(Unit))
    }

}