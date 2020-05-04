package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.EndpointHandler

fun dispatch(handler: CommandDispatcher.() -> EndpointHandler): ExpressHandler = { request, response ->
    request.commandDispatcher.handler()
        .invoke(request, response)
}