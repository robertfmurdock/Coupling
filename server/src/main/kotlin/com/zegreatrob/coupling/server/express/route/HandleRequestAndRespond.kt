package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.action.CommandExecuteSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.launch

fun <T> handleRequestAndRespond(
    request: Request,
    response: Response,
    handler: suspend CommandExecuteSyntax.() -> T,
    responder: Response.(T) -> Unit
) = request.scope.launch {
    runCatching {
        val result = object : CommandExecuteSyntax {}.handler()
        response.responder(result)
    }.getOrElse { error ->
        request.commandDispatcher.logger.error(error) { "EXCEPTION!" }
        response.sendStatus(500)
    }
}