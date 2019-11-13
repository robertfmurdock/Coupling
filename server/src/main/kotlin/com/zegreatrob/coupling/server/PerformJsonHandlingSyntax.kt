package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import kotlinx.coroutines.promise
import kotlin.js.Json
import kotlin.reflect.KSuspendFunction1

interface PerformJsonHandlingSyntax : ScopeSyntax {
    fun performJsonHandling(request: Request, responder: (Json) -> Unit, handler: KSuspendFunction1<Request, Json>) =
        scope.promise {
            val json = handler(request)
            responder(json)
        }
}
