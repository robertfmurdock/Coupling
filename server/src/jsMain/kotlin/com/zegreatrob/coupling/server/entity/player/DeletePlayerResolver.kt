package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.json.DeletePlayerInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val deletePlayerResolver = dispatch(
    { request, _: JsonNull, args -> authorizedPartyDispatcher(context = request, partyId = args.partyId.value) },
    { _, input: DeletePlayerInput -> input.toCommand() },
    { true },
)

private fun DeletePlayerInput.toCommand() = DeletePlayerCommand(partyId, playerId)
