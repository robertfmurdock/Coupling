package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class SavePinCommand(val id: PartyId, val pin: Pin) {
    fun interface Dispatcher {
        suspend fun perform(command: SavePinCommand): VoidResult
    }
}
