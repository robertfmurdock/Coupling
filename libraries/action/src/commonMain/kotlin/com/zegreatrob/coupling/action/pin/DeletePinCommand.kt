package com.zegreatrob.coupling.action.pin

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DeletePinCommand(val partyId: PartyId, val pinId: String) :
    SimpleSuspendAction<DeletePinCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePinCommand): VoidResult
    }
}