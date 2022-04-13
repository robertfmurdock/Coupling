package com.zegreatrob.coupling.server.graphql

import com.soywiz.klock.TimeProvider
import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.repository.dynamo.DynamoBoostRepository
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentPartyIdDispatcher
import com.zegreatrob.coupling.server.ICommandDispatcher
import com.zegreatrob.coupling.server.PrereleaseDispatcher
import com.zegreatrob.coupling.server.express.Config

object DispatcherProviders {
    val command: GraphQLDispatcherProvider<CommandDispatcher> = { r, _, _ -> r.commandDispatcher }
    val tribeCommand: GraphQLDispatcherProvider<CurrentPartyIdDispatcher> = { request, entity, args ->
        val tribeId = entity?.get("id").unsafeCast<String?>()
            ?: (args as? TribeInput)?.tribeId?.value
            ?: ""
        request.commandDispatcher
            .authorizedTribeIdDispatcher(tribeId)
            .let { if (it.isAuthorized()) it else null }
    }

    val prereleaseCommand: GraphQLDispatcherProvider<PrereleaseDispatcher> = { request, entity, args ->
        val dispatcher = command(request, entity, args)

        if (dispatcher == null || !Config.prereleaseMode)
            null
        else {
            val boostRepo = DynamoBoostRepository(dispatcher.user.id, TimeProvider)
            object : PrereleaseDispatcher, ICommandDispatcher by dispatcher {
                override val boostRepository = boostRepo
                override val userId = dispatcher.user.id

                override suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message) =
                    dispatcher.sendMessageAndReturnIdWhenFail(connectionId, message)
            }
        }
    }
}

