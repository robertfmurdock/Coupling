package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.external.express.Handler
import com.zegreatrob.coupling.server.external.express.Next
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun apiGuard(): Handler = { request, response, next ->
    if (request.isAuthenticated != true) {
        response.sendStatus(401)
    } else {
        request.scope.launch(block = setupDispatcher(request, next))
    }
}

private fun setupDispatcher(request: Request, next: Next): suspend CoroutineScope.() -> Unit = {
    val commandDispatcher = request.commandDispatcher()
    request.asDynamic().commandDispatcher = commandDispatcher
    next()
}

suspend fun Request.commandDispatcher() = com.zegreatrob.coupling.server.commandDispatcher(user, scope, traceId)
