package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.PinId
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class DeletePinCommand(val partyId: PartyId, val pinId: PinId) {
    fun interface Dispatcher {
        suspend fun perform(command: DeletePinCommand): VoidResult
    }
}
