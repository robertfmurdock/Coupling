package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.reflect.KSuspendFunction1

interface PerformJsonHandlingSyntax : ScopeSyntax {
    fun <T> performJsonHandling(
        request: Request,
        response: Response,
        responder: Response.(T) -> Unit,
        handler: KSuspendFunction1<Request, T>
    ) =
        scope.promise {
            val json = handler(request)
            response.responder(json)
        }
}
