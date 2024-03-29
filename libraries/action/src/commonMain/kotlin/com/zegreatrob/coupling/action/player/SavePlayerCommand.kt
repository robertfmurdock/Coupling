package com.zegreatrob.coupling.action.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SavePlayerCommand(val partyId: PartyId, val player: Player) {
    fun interface Dispatcher {
        suspend fun perform(command: SavePlayerCommand): VoidResult
    }
}
