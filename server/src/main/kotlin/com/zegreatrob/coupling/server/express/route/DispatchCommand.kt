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
    with(request.commandDispatcher) {
        handleRequestAndRespond(request, response, {
            toResult(
                with(dispatcher(request)) {
                    command.logAsync { command.execute(this) }
                }

            )
        }, responder)
    }
}
