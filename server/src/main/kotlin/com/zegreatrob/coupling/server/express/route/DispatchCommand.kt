package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.SuspendResultAction
import com.zegreatrob.coupling.actionFunc.CommandExecuteSyntax
import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.external.express.ExpressHandler
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.launch

typealias ExpressDispatcherProvider<D> = (Request) -> D

fun <C : SuspendResultAction<D, R>, D, R, J> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C,
    toJson: (R) -> J
): ExpressHandler = { request, response ->
    val command = toCommandFunc(request)
    handleRequestAndRespond(
        request,
        response,
        { dispatcher(request).execute(command) },
        { ResponseHelpers.response(this, it, toJson) })
}

fun <C : SuspendResultAction<D, R>, D, R> dispatch(
    dispatcher: (Request) -> D,
    toCommandFunc: (Request) -> C
): ExpressHandler = dispatch(dispatcher, toCommandFunc, {})

private fun <T> handleRequestAndRespond(
    request: Request,
    response: Response,
    handler: suspend CommandExecuteSyntax.() -> T,
    responder: Response.(T) -> Unit
) = request.scope.launch {
    runCatching {
        val result = request.commandDispatcher.handler()
        response.responder(result)
    }.getOrElse { error ->
        request.commandDispatcher.logger.error(error) { "EXCEPTION!" }
        response.sendStatus(500)
    }
}