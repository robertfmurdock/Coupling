package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePlayerCommand(val partyId: PartyId, val player: Player) :
    SimpleSuspendAction<SavePlayerCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePlayerCommand)
    }
}
