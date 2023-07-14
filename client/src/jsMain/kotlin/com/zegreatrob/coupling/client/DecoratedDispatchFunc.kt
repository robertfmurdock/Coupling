package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.LoggingActionPipe
import com.zegreatrob.coupling.action.TraceIdProvider
import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.testmints.action.ActionCannon
import com.zegreatrob.testmints.action.async.SuspendAction

class DecoratedDispatchFunc<D : TraceIdProvider>(
    val dispatcherFunc: () -> D,
    private val tools: DataLoaderTools,
) : DispatchFunc<D> {

    private val dispatcher get() = dispatcherFunc()

    override fun <C : SuspendAction<D, R>, R> invoke(commandFunc: () -> C, response: (R) -> Unit) = fun() {
        val command = commandFunc()
        tools.performAsyncWork(
            { command.execute(dispatcher) },
            { handler: Throwable -> throw handler },
            response,
        )
    }

    override fun <C, R> invoke(
        commandFunc: () -> C,
        fireCommand: suspend ActionCannon<D>.(C) -> R,
        response: (R) -> Unit,
    ): () -> Unit = fun() {
        val cannon = ActionCannon(dispatcher, LoggingActionPipe(dispatcher.traceId))
        val command = commandFunc()
        tools.performAsyncWork(
            { fireCommand(cannon, command) },
            { handler: Throwable -> throw handler },
            response,
        )
    }
}
