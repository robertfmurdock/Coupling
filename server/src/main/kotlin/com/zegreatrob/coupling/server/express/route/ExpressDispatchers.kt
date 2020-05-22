package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.ActionDispatcher
import com.zegreatrob.coupling.server.external.express.Request

object ExpressDispatchers {
    val command: ExpressDispatcherProvider<ActionDispatcher> = ::commandDispatcher
}

fun commandDispatcher(request: Request): ActionDispatcher = request.commandDispatcher