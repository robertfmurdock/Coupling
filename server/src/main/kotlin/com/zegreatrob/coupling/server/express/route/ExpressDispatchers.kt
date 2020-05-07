package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request

object ExpressDispatchers {
    val command: ExpressDispatcherProvider<CommandDispatcher> = ::commandDispatcher
}

fun commandDispatcher(request: Request): CommandDispatcher = request.commandDispatcher