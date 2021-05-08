package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher

object DispatcherProviders {
    val command: GraphQLDispatcherProvider<CommandDispatcher> = { r, _ -> r.commandDispatcher }
    val tribeCommand: GraphQLDispatcherProvider<CurrentTribeIdDispatcher> = { request, entity ->
        request.commandDispatcher
            .authorizedTribeIdDispatcher(entity["id"].toString())
            .let { if (it.isAuthorized()) it else null }
    }
}