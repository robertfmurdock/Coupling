package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DeletePinCommand(val partyId: PartyId, val pinId: String) {
    fun interface Dispatcher {
        suspend fun perform(command: DeletePinCommand): VoidResult
    }
}
