package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.action.LoggingSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun <T> LoggingSyntax.handleRequestAndRespond(
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