package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePartyCommand(val party: PartyDetails) : SimpleSuspendAction<SavePartyCommand.Dispatcher, VoidResult> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePartyCommand): VoidResult
    }
}
