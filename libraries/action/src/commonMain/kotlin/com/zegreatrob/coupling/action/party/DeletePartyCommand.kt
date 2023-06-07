package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class DeletePartyCommand(val partyId: PartyId) : SimpleSuspendAction<DeletePartyCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePartyCommand): VoidResult
    }
}
