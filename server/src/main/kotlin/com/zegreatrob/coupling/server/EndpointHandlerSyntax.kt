package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.Promise

interface EndpointHandlerSyntax : ScopeSyntax {
    fun <T> endpointHandler(responder: Response.(T) -> Unit, handler: suspend Request.() -> T): EndpointHandler =
        { request: Request, response: Response ->
            scope.promise {
                val result = handler(request)
                response.responder(result)
            }
        }
}

typealias EndpointHandler = (Request, Response) -> Promise<Unit>
