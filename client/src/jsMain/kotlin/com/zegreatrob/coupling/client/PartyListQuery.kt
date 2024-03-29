package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

object PartyListQuery : SimpleSuspendAction<PartyListQuery.Dispatcher, List<PartyDetails>?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyListQuery): List<PartyDetails>
    }
}
