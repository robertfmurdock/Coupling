package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object PartyListQuery : SimpleSuspendAction<PartyListQuery.Dispatcher, List<Party>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyListQuery): List<Party>
    }
}
