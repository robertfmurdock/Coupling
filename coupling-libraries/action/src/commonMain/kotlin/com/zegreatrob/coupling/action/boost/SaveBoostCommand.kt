package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.SuccessfulResult
import com.zegreatrob.coupling.model.party.PartyId

data class SaveBoostCommand(val partyIds: Set<PartyId>) :
    SimpleSuspendResultAction<SaveBoostCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SaveBoostCommand): SuccessfulResult<Unit>
    }
}
