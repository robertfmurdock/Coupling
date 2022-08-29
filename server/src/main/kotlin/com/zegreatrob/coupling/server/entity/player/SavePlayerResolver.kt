package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.SavePlayerInput
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val savePlayerResolver =
    dispatch(partyCommand, { _, args: SavePlayerInput -> SavePlayerCommand(args.toModel()) }, { true })
