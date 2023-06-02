package com.zegreatrob.coupling.server.graphql

import com.zegreatrob.coupling.json.JsonPartyData
import com.zegreatrob.coupling.model.Message
import com.zegreatrob.coupling.repository.dynamo.DynamoBoostRepository
import com.zegreatrob.coupling.server.CommandDispatcher
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.ICommandDispatcher
import com.zegreatrob.coupling.server.PrereleaseDispatcher
import com.zegreatrob.coupling.server.express.Config
import com.zegreatrob.coupling.server.external.express.Request
import korlibs.time.TimeProvider
import kotlinx.serialization.json.JsonNull

object DispatcherProviders {
    fun <E, I> command(): GraphQLDispatcherProvider<E, I, CommandDispatcher> = { r, _, _ -> r.commandDispatcher }
    val partyCommand: GraphQLDispatcherProvider<JsonPartyData, JsonNull, CurrentPartyDispatcher> =
        { request, entity, _ -> authorizedDispatcher(request = request, partyId = entity.id ?: throw Exception("Party not found")) }

    suspend fun authorizedDispatcher(
        request: Request,
        partyId: String,
    ) = request.commandDispatcher.authorizedPartyIdDispatcher(partyId).let { if (it.isAuthorized()) it else null }

    fun <E, I> prereleaseCommand(): GraphQLDispatcherProvider<E, I, PrereleaseDispatcher> = { request, entity, args ->
        val dispatcher = command<E, I>()(request, entity, args)

        if (dispatcher == null || !Config.prereleaseMode) {
            null
        } else {
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
