package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.ActionDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher

object DispatcherProviders {
    val command: GraphQLDispatcherProvider<ActionDispatcher> = { r, _ -> r.commandDispatcher }
    val tribeCommand: GraphQLDispatcherProvider<CurrentTribeIdDispatcher> = { request, entity ->
        request.commandDispatcher
            .authorizedTribeIdDispatcher(entity.tribeId())
            .let { if (it.isAuthorized()) it else null }
    }
}