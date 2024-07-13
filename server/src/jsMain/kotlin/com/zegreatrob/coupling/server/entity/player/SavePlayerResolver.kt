package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.action.player.perform
import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.CurrentPartyDispatcher
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedPartyDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePlayerResolver = dispatch<JsonNull, SavePlayerInput, CurrentPartyDispatcher, SavePlayerCommand, VoidResult, Boolean>(
    dispatcherFunc = requiredInput { request, _: JsonNull, args ->
        authorizedPartyDispatcher(
            context = request,
            partyId = args.partyId.value,
        )
    },
    commandFunc = requiredInput { _, args: SavePlayerInput -> args.command() },
    fireFunc = ::perform,
    toSerializable = { true },
)

private fun SavePlayerInput.command() = SavePlayerCommand(partyId, toModel())
