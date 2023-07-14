package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.MintAction

@MintAction
data class DeletePinCommand(val partyId: PartyId, val pinId: String) {
    interface Dispatcher {
        suspend fun perform(command: DeletePinCommand): VoidResult
    }
}
