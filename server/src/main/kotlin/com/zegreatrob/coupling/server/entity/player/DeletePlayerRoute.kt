package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.command
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlin.js.Json

val deletePlayerResolver = dispatch(command, { _, args ->
    val input = args["input"].unsafeCast<Json>()
    val tribeId = TribeId(input["tribeId"].toString())
    val playerId = input["playerId"].toString()
    DeletePlayerCommand(tribeId, playerId)
}, { true })

