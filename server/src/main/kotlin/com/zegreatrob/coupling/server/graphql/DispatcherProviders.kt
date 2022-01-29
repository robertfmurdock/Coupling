package com.zegreatrob.coupling.server.graphql

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.dynamo.DynamoBoostRepository
import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentTribeIdDispatcher
import com.zegreatrob.coupling.server.ICurrentTribeIdDispatcher
import com.zegreatrob.coupling.server.PrereleaseTribeIdDispatcher
import com.zegreatrob.coupling.server.express.Config

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

    val prereleaseTribeCommand: GraphQLDispatcherProvider<PrereleaseTribeIdDispatcher> = { request, entity, args ->
        val dispatcher = tribeCommand(request, entity, args)

        if (dispatcher == null || !Config.prereleaseMode)
            null
        else {
            val boostRepo = DynamoBoostRepository(dispatcher.userId, TimeProvider)

            object : PrereleaseTribeIdDispatcher, ICurrentTribeIdDispatcher by dispatcher {
                override val boostRepository = boostRepo
                override val userId = dispatcher.userId

                override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message) =
                    dispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)
            }
        }
    }
}

