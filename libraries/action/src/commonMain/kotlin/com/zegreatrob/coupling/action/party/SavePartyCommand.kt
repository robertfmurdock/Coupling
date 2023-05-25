package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.model.party.Party

data class SavePartyCommand(val party: Party) :
    com.zegreatrob.testmints.action.async.SimpleSuspendAction<SavePartyCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePartyCommand)
    }
}
