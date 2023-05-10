package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyCurrentDataQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyCurrentDataQuery.Dispatcher, PartyDataMost?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: PartyCurrentDataQuery): PartyDataMost?
    }
}

interface ClientPartyCurrentDataQueryDispatcher : PartyLoadMostSyntax, PartyCurrentDataQuery.Dispatcher {
    override suspend fun perform(query: PartyCurrentDataQuery) = query.partyId.loadMost()
}
