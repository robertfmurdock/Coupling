package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.Result

class StubDispatchFunc<D> : DispatchFunc<D> {

    val dispatchList = mutableListOf<DispatchedFunc<*, *>>()

    override fun <C, R> makeItSo(
        response: (Result<R>) -> Unit,
        buildCommand: () -> C,
        execute: suspend C.(D) -> Result<R>
    ): () -> Unit = { dispatchList.add(DispatchedFunc(buildCommand(), response)) }

    data class DispatchedFunc<C, R>(val command: C, val responseFunc: (Result<R>) -> Unit)

}