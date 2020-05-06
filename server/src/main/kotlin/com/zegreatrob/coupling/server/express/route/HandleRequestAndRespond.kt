package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.launch

fun <T> handleRequestAndRespond(
    request: Request,
    response: Response,
    handler: suspend Request.() -> T,
    responder: Response.(T) -> Unit
) = request.scope.launch {
    runCatching {
        val result = request.handler()
        response.responder(result)
    }.getOrElse { error ->
        request.commandDispatcher.logger.error(error) { "EXCEPTION!" }
        response.sendStatus(500)
    }
}