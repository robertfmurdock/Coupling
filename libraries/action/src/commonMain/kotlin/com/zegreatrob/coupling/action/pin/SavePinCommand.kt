package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePinCommand(val id: PartyId, val pin: Pin) :
    SimpleSuspendAction<SavePinCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePinCommand): VoidResult
    }
}
