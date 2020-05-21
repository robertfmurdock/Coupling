package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.actionFunc.SimpleSuspendResultAction
import com.zegreatrob.coupling.actionFunc.deletionResult
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommandDispatcher, Unit> {
    override val performFunc = link(DeletePlayerCommandDispatcher::perform)
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax {

    suspend fun perform(command: DeletePlayerCommand) = command.tribeIdPlayerId()
        .deletePlayer()
        .deletionResult("player")

    private fun DeletePlayerCommand.tribeIdPlayerId(): TribeIdPlayerId = TribeIdPlayerId(
        tribeId,
        playerId
    )

}