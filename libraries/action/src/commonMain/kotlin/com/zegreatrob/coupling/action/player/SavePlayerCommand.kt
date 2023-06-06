package com.zegreatrob.coupling.action.player

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player

data class SavePlayerCommand(val partyId: PartyId, val player: Player) :
    SimpleSuspendResultAction<SavePlayerCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePlayerCommand): Result<Unit>
    }
}
