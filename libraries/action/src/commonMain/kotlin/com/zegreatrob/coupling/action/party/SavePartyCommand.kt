package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SavePartyCommand(val party: PartyDetails) {
    interface Dispatcher {
        suspend fun perform(command: SavePartyCommand): VoidResult
    }
}
