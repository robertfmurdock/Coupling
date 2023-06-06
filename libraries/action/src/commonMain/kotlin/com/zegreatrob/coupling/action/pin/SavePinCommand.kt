package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.pin.Pin

data class SavePinCommand(val id: PartyId, val pin: Pin) :
    SimpleSuspendResultAction<SavePinCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePinCommand): Result<Unit>
    }
}
