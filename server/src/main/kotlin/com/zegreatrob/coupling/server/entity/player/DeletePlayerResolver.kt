package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.couplingJsonFormat
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import com.zegreatrob.minjson.at
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromDynamic

val deletePlayerResolver = dispatch(
    tribeCommand,
    { _, args ->
        val input = couplingJsonFormat.decodeFromDynamic<DeletePlayerInput>(args.at("/input"))
        DeletePlayerCommand(input.playerId)
    },
    { true }
)

@Serializable
data class DeletePlayerInput(
    val playerId: String,
    val tribeId: String
)
