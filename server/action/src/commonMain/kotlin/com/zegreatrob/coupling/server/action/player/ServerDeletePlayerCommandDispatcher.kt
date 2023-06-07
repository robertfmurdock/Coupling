package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.PartyPlayerIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

interface ServerDeletePlayerCommandDispatcher :
    DeletePlayerCommand.Dispatcher,
    PartyPlayerIdDeleteSyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: DeletePlayerCommand) = command.partyIdPlayerId()
        .run { deletePlayer() }
        .voidResult()

    private fun DeletePlayerCommand.partyIdPlayerId() = PartyIdPlayerId(currentPartyId, playerId)
}
