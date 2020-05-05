package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface EndpointHandlerSyntax : LoggingSyntax {
    fun <T> endpointHandler(responder: Response.(T) -> Unit, handler: suspend Request.() -> T): EndpointHandler =
        { request: Request, response: Response ->
            handleRequestAndRespond(request, response, handler, responder)
        }

    private fun <T> handleRequestAndRespond(
        request: Request,
        response: Response,
        handler: suspend Request.() -> T,
        responder: Response.(T) -> Unit
    ) = request.scope.launch {
        runCatching {
            val result = request.handler()
            response.responder(result)
        }.getOrElse { error ->
            logger.error(error) { "EXCEPTION!" }
            response.sendStatus(500)
        }
    }
}

typealias EndpointHandler = (Request, Response) -> Job
