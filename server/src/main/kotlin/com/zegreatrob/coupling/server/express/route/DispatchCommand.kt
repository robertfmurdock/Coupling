package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.external.express.ExpressHandler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful

fun <C : SuspendAction<D, R>, D, R, J> dispatchCommand(
    toCommandFunc: (Request) -> C,
    dispatcher: (Request) -> D,
    toResult: (R) -> J,
    responder: Response.(J) -> Unit = Response::sendSuccessful
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    handleRequestAndRespond(request, response, {
        loggedExecute(request, dispatcher, command, toResult)
    }, responder)
}

suspend fun <D, C : SuspendAction<D, R>, R, J> loggedExecute(
    request: Request,
    dispatcher: (Request) -> D,
    command: C,
    toResult: (R) -> J
) = with(request.commandDispatcher) {
    toResult(
        command.logAsync { command.execute(dispatcher(request)) }
    )
}