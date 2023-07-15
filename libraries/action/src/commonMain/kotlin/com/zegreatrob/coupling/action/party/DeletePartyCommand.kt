package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class DeletePartyCommand(val partyId: PartyId) {
    interface Dispatcher {
        suspend fun perform(command: DeletePartyCommand): VoidResult
    }
}
