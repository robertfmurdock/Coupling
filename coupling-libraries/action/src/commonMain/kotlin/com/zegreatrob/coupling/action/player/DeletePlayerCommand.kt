package com.zegreatrob.coupling.action.player

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.PartyId

data class DeletePlayerCommand(val partyId: PartyId, val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePlayerCommand): Result<Unit>
    }
}
