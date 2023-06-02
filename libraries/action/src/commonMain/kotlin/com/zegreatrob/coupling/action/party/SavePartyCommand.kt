package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePartyCommand(val party: Party) : SimpleSuspendAction<SavePartyCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePartyCommand)
    }
}
