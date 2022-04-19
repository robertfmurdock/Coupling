package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PartyPlayerSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePlayerCommand(val partyId: PartyId, val player: Player) :
    SimpleSuspendAction<SavePlayerCommandDispatcher, Unit> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : PartyPlayerSaveSyntax {
    suspend fun perform(command: SavePlayerCommand) = command.partyIdPlayer().save()

    private fun SavePlayerCommand.partyIdPlayer() = partyId.with(player)
}
