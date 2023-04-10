package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.PartyId

data class DeletePinCommand(val id: PartyId, val pinId: String) :
    SimpleSuspendResultAction<DeletePinCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePinCommand): Result<Unit>
    }
}
