package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.stats.PartyDataMost
import com.zegreatrob.coupling.client.stats.PartyLoadMostSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class PartyCurrentDataQuery(val partyId: PartyId) :
    SimpleSuspendAction<PartyCurrentDataQueryDispatcher, PartyDataMost?> {
    override val performFunc = link(PartyCurrentDataQueryDispatcher::perform)
}

interface PartyCurrentDataQueryDispatcher : PartyLoadMostSyntax {
    suspend fun perform(query: PartyCurrentDataQuery) = query.partyId.loadMost()
}
