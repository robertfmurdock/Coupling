package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.player.TribeIdPlayerSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePlayerCommand(val tribeId: PartyId, val player: Player) :
    SimpleSuspendAction<SavePlayerCommandDispatcher, Unit> {
    override val performFunc = link(SavePlayerCommandDispatcher::perform)
}

interface SavePlayerCommandDispatcher : TribeIdPlayerSaveSyntax {
    suspend fun perform(command: SavePlayerCommand) = command.tribeIdPlayer().save()

    private fun SavePlayerCommand.tribeIdPlayer() = tribeId.with(player)
}
