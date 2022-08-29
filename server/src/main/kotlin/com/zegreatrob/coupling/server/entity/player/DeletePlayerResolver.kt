package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.DeletePlayerInput
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val deletePlayerResolver = dispatch(
    partyCommand,
    { _, input: DeletePlayerInput -> DeletePlayerCommand(input.playerId) },
    { true }
)
