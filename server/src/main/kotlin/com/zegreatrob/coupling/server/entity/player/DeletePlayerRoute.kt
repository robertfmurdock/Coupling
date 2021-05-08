package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val deletePlayerResolver = dispatch(tribeCommand, { _, args ->
    val input = args["input"].unsafeCast<Json>()
    val playerId = input["playerId"].toString()
    DeletePlayerCommand(playerId)
}, { true })
