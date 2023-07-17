package com.zegreatrob.coupling.action.player

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DeletePlayerCommand(val partyId: PartyId, val playerId: String) {
    interface Dispatcher {
        suspend fun perform(command: DeletePlayerCommand): VoidResult
    }
}
