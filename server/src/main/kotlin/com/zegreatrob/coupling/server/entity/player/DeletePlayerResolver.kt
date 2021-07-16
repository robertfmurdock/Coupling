package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.TribeInput
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.Serializable

val deletePlayerResolver = dispatch(
    tribeCommand,
    { _, input: DeletePlayerInput -> DeletePlayerCommand(input.playerId) },
    { true }
)

@Serializable
data class DeletePlayerInput(
    val playerId: String,
    override val tribeId: String
) : TribeInput
