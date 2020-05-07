package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.external.express.ExpressHandler
import com.zegreatrob.coupling.server.external.express.Request

typealias ExpressDispatcherProvider<D> = (Request) -> D

fun <C : SuspendAction<D, R>, D, R, J> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C,
    toJson: (R) -> J
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    handleRequestAndRespond(request, response, {
        request.commandDispatcher.loggedExecute(dispatcher(request), command)
    }, { ResponseHelpers.response(this, it, toJson) })
}

fun <C : SuspendAction<D, R>, D, R> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C
): ExpressHandler = dispatch(dispatcher, toCommandFunc, {})

suspend fun <D, C : SuspendAction<D, R>, R> ActionLoggingSyntax.loggedExecute(
    dispatcher: D,
    command: C
) = command.logAsync { command.execute(dispatcher) }
