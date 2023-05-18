package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.PartyPlayerIdDeleteSyntax

interface ClientDeletePlayerCommandDispatcher : PartyPlayerIdDeleteSyntax, DeletePlayerCommand.Dispatcher {

    override suspend fun perform(command: DeletePlayerCommand) = command.partyPlayerId()
        .deletePlayer()
        .deletionResult("player")

    private fun DeletePlayerCommand.partyPlayerId(): PartyIdPlayerId = PartyIdPlayerId(
        partyId,
        playerId,
    )
}
