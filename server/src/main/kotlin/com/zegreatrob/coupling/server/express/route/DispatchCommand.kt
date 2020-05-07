package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.external.express.ExpressHandler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful

fun <C : SuspendAction<D, R>, D, R, J> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C,
    toResult: (R) -> J,
    responder: Response.(J) -> Unit = Response::sendSuccessful
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    handleRequestAndRespond(request, response, {
        loggedExecute(request, command, toResult, dispatcher(request))
    }, responder)
}

suspend fun <D, C : SuspendAction<D, R>, R, J> loggedExecute(
    request: Request,
    command: C,
    toResult: (R) -> J,
    dispatcher: D
) = with(request.commandDispatcher) {
    toResult(
        command.logAsync { command.execute(dispatcher) }
    )
}
