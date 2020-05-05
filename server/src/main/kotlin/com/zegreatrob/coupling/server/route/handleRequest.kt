package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.EndpointHandler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import com.zegreatrob.coupling.server.handleRequestAndRespond

fun dispatchCommand(handler: CommandDispatcher.() -> EndpointHandler): ExpressHandler = { request, response ->
    request.commandDispatcher.handler()
        .invoke(request, response)
}

fun <C, R> dispatchCommand(
    toCommandFunc: (Request) -> C,
    dispatch: suspend CommandDispatcher.(C) -> R,
    toResult: (R) -> Any?
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    with(request.commandDispatcher) {
        handleRequestAndRespond(request, response, { toResult(dispatch(command)) }, Response::sendSuccessful)
    }
}