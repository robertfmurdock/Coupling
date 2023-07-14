package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class SaveBoostCommand(val partyIds: Set<PartyId>) {
    interface Dispatcher {
        suspend fun perform(command: SaveBoostCommand): VoidResult
    }
}
