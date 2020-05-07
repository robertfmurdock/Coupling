package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.external.express.ExpressHandler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful

typealias ExpressDispatcherProvider<D> = (Request) -> D

fun <C : SuspendAction<D, R>, D, R, J> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C,
    toResult: (R) -> J,
    responder: Response.(J) -> Unit = Response::sendSuccessful
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    handleRequestAndRespond(request, response, {
        request.commandDispatcher.loggedExecute(dispatcher(request), command, toResult)
    }, responder)
}

suspend fun <D, C : SuspendAction<D, R>, R, J> ActionLoggingSyntax.loggedExecute(
    dispatcher: D,
    command: C,
    toResult: (R) -> J
) = toResult(
    command.logAsync { command.execute(dispatcher) }
)
