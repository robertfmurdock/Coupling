package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax

data class DeletePlayerCommand(val tribeId: TribeId, val playerId: String) :
    SimpleSuspendAction<DeletePlayerCommandDispatcher, Unit> {
    override val perform = link(DeletePlayerCommandDispatcher::perform)
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax {
    suspend fun perform(command: DeletePlayerCommand) = command.tribeIdPlayerId()
        .run { deletePlayer() }
        .deletionResult("Player")

    private fun DeletePlayerCommand.tribeIdPlayerId() = TribeIdPlayerId(tribeId, playerId)
}
