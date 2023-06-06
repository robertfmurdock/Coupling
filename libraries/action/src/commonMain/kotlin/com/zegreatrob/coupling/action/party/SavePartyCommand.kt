package com.zegreatrob.coupling.action.party

import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.model.party.Party

data class SavePartyCommand(val party: Party) : SimpleSuspendResultAction<SavePartyCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: SavePartyCommand): Result<Unit>
    }
}
