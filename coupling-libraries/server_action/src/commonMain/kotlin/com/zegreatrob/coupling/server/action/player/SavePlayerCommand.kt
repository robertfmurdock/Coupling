package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

data class SavePlayerCommand(val player: Player) : SimpleSuspendResultAction<SavePlayerCommandDispatcher, Player> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : PartyPlayerSaveSyntax, CurrentPartyIdSyntax {

    suspend fun perform(command: SavePlayerCommand) = command.sldkfjldksjf().successResult()

    private suspend fun SavePlayerCommand.sldkfjldksjf() = currentPartyId.with(player)
        .apply { save() }
        .run { player }

}
