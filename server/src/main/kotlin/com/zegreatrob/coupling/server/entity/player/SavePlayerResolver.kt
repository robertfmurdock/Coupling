package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.action.player.SavePlayerCommand
import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.authorizedDispatcher
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val savePlayerResolver = dispatch(
    dispatcherFunc = { request, _: JsonNull, args ->
        authorizedDispatcher(
            request = request,
            partyId = args.partyId.value,
        )
    },
    queryFunc = { _, args: SavePlayerInput -> args.command() },
    toSerializable = { true },
)

private fun SavePlayerInput.command() = SavePlayerCommand(partyId, toModel())
