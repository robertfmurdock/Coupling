package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.DispatchFunc
import com.zegreatrob.react.dataloader.DataLoaderTools
import com.zegreatrob.testmints.action.async.SuspendAction
import com.zegreatrob.testmints.action.async.SuspendActionExecuteSyntax

class DecoratedDispatchFunc<D : SuspendActionExecuteSyntax>(
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
}
