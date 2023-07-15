package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.async.SuspendAction

class DecoratedDispatchFunc<D>(
    val cannonFunc: () -> ActionCannon<D>,
    private val tools: DataLoaderTools,
) : DispatchFunc<D> {

    override fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (R) -> Unit) = fun() {
        val cannon = cannonFunc()
        val command = commandFunc()
        tools.performAsyncWork(
            { cannon.fire(command) },
            { handler: Throwable -> throw handler },
            response,
        )
    }

    override fun <C, R> invoke(
        commandFunc: () -> C,
        fireFunc: suspend ActionCannon<D>.(C) -> R,
        response: (R) -> Unit,
    ): () -> Unit = fun() {
        val cannon = cannonFunc()
        val command = commandFunc()
        tools.performAsyncWork(
            work = { fireFunc(cannon, command) },
            errorResult = { handler: Throwable -> throw handler },
            onWorkComplete = response,
        )
    }

    override fun invoke(block: suspend ActionCannon<D>.() -> Unit): () -> Unit = fun() {
        val cannon = cannonFunc()
        tools.performAsyncWork(
            work = { block(cannon) },
            errorResult = { handler: Throwable -> throw handler },
            onWorkComplete = {},
        )
    }
}
