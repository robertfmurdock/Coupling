package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) :
    SuspendAction<DeletePlayerCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePlayerCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax {

    suspend fun DeletePlayerCommand.perform() = TribeIdPlayerId(tribeId, playerId)
        .deletePlayer()
        .deletionResult("player")

}