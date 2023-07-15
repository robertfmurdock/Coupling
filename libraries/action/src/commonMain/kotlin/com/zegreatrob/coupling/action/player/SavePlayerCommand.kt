package com.zegreatrob.coupling.action.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class SavePlayerCommand(val partyId: PartyId, val player: Player) {
    interface Dispatcher {
        suspend fun perform(command: SavePlayerCommand): VoidResult
    }
}
