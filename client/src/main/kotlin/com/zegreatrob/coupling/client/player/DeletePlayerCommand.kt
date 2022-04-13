package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.player.TribeIdPlayerIdDeleteSyntax

data class DeletePlayerCommand(val tribeId: PartyId, val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommandDispatcher, Unit> {
    override val performFunc = link(DeletePlayerCommandDispatcher::perform)
}

interface DeletePlayerCommandDispatcher : TribeIdPlayerIdDeleteSyntax {

    suspend fun perform(command: DeletePlayerCommand) = command.tribeIdPlayerId()
        .deletePlayer()
        .deletionResult("player")

    private fun DeletePlayerCommand.tribeIdPlayerId(): PartyIdPlayerId = PartyIdPlayerId(
        tribeId,
        playerId
    )

}