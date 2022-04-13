package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.stats.TribeDataMost
import com.zegreatrob.coupling.client.stats.PartyLoadMostSyntax
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeCurrentDataQuery(val tribeId: PartyId) :
    SimpleSuspendAction<PartyCurrentDataQueryDispatcher, TribeDataMost?> {
    override val performFunc = link(PartyCurrentDataQueryDispatcher::perform)
}

interface PartyCurrentDataQueryDispatcher : PartyLoadMostSyntax {
    suspend fun perform(query: TribeCurrentDataQuery) = query.tribeId.loadMost()
}