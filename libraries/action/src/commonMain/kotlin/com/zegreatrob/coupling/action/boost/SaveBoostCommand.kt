package com.zegreatrob.coupling.action.boost

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SaveBoostCommand(val partyIds: Set<PartyId>) :
    SimpleSuspendAction<SaveBoostCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SaveBoostCommand): VoidResult
    }
}
