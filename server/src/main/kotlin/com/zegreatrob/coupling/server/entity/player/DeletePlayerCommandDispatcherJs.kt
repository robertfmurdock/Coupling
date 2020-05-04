package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.EndpointHandlerSyntax
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.external.express.tribeId

interface DeletePlayerCommandDispatcherJs : DeletePlayerCommandDispatcher, RequestPlayerIdSyntax,
    EndpointHandlerSyntax {

    val performDeletePlayerCommand
        get() = endpointHandler(sendDeleteResults("Player")) {
            DeletePlayerCommand(tribeId(), playerId())
                .perform()
        }
}
