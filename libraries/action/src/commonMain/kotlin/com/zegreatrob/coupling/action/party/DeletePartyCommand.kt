package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.PartyId

data class DeletePartyCommand(val partyId: PartyId) :
    SimpleSuspendResultAction<DeletePartyCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePartyCommand): Result<Unit>
    }
}
