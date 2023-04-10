package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.action.SavePlayerCommand
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax

interface ClientSavePlayerCommandDispatcher : PartyPlayerSaveSyntax, SavePlayerCommand.Dispatcher {
    override suspend fun perform(command: SavePlayerCommand) = command.partyIdPlayer().save()

    private fun SavePlayerCommand.partyIdPlayer() = partyId.with(player)
}
