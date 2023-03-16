package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.PartyPlayerIdDeleteSyntax

data class DeletePlayerCommand(val partyId: PartyId, val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommandDispatcher, Unit> {
    override val performFunc = link(DeletePlayerCommandDispatcher::perform)
}

interface DeletePlayerCommandDispatcher : PartyPlayerIdDeleteSyntax {

    suspend fun perform(command: DeletePlayerCommand) = command.partyPlayerId()
        .deletePlayer()
        .deletionResult("player")

    private fun DeletePlayerCommand.partyPlayerId(): PartyIdPlayerId = PartyIdPlayerId(
        partyId,
        playerId,
    )
}
