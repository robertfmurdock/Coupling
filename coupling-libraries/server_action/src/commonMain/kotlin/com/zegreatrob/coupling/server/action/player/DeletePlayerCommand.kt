package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class DeletePlayerCommand(val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommandDispatcher, Unit> {
    override val performFunc = link(DeletePlayerCommandDispatcher::perform)
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax, CurrentTribeIdSyntax {
    suspend fun perform(command: DeletePlayerCommand) = command.tribeIdPlayerId()
        .run { deletePlayer() }
        .deletionResult("Player")

    private fun DeletePlayerCommand.tribeIdPlayerId() = PartyIdPlayerId(currentPartyId, playerId)
}
