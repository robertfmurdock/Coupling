package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.perform
import com.zegreatrob.coupling.json.GqlDeletePlayerInput
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull
import org.kotools.types.ExperimentalKotoolsTypesApi

val deletePlayerResolver = dispatch(
    dispatcherFunc = requiredInput { request, _, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId,
        )
    },
    commandFunc = requiredInput { _: JsonNull, input: GqlDeletePlayerInput -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

@OptIn(ExperimentalKotoolsTypesApi::class)
private fun GqlDeletePlayerInput.toCommand() = DeletePlayerCommand(partyId, playerId)
