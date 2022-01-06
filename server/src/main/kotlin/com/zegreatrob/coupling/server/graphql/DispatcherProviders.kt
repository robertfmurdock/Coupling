package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher

object DispatcherProviders {
    val command: GraphQLDispatcherProvider<CommandDispatcher> = { r, _, _ -> r.commandDispatcher }
    val tribeCommand: GraphQLDispatcherProvider<CurrentTribeIdDispatcher> = { request, entity, args ->
        val tribeId = entity?.get("id").unsafeCast<String?>()
            ?: (args as? TribeInput)?.tribeId
            ?: ""
        request.commandDispatcher
            .authorizedTribeIdDispatcher(tribeId)
            .let { if (it.isAuthorized()) it else null }
    }
}
