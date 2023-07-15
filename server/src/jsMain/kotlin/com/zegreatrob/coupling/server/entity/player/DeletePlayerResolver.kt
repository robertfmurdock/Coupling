package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.player.perform
import com.zegreatrob.coupling.json.DeletePlayerInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatchAction
import kotlinx.serialization.json.JsonNull

val deletePlayerResolver = dispatchAction(
    dispatcherFunc = { request, _: JsonNull, args -> authorizedPartyDispatcher(context = request, partyId = args.partyId.value) },
    commandFunc = { _, input: DeletePlayerInput -> input.toCommand() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun DeletePlayerInput.toCommand() = DeletePlayerCommand(partyId, playerId)
