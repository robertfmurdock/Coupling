package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.model.party.PartyId

data class DeletePlayerCommand(val partyId: PartyId, val playerId: String) :
    SimpleSuspendResultAction<DeletePlayerCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeletePlayerCommand): Result<Unit>
    }
}
