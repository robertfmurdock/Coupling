package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.player.PartyIdPlayerId
import com.zegreatrob.coupling.repository.player.PartyPlayerIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

data class DeletePlayerCommand(val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PartyPlayerIdDeleteSyntax, CurrentPartyIdSyntax {
        suspend fun perform(command: DeletePlayerCommand) = command.partyIdPlayerId()
            .run { deletePlayer() }
            .deletionResult("Player")

        private fun DeletePlayerCommand.partyIdPlayerId() = PartyIdPlayerId(currentPartyId, playerId)
    }
}
