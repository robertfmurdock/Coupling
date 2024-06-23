package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.PartyPlayerIdDeleteSyntax

interface ClientDeletePlayerCommandDispatcher :
    PartyPlayerIdDeleteSyntax,
    DeletePlayerCommand.Dispatcher {

    override suspend fun perform(command: DeletePlayerCommand) = command.partyPlayerId()
        .deletePlayer()
        .voidResult()

    private fun DeletePlayerCommand.partyPlayerId(): PartyIdPlayerId = PartyIdPlayerId(
        partyId,
        playerId,
    )
}
