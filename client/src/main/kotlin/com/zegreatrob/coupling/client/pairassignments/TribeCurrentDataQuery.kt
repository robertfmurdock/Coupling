package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.stats.TribeDataMost
import com.zegreatrob.coupling.client.stats.TribeIdLoadMostSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeCurrentDataQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribeCurrentDataQueryDispatcher, TribeDataMost?> {
    override val performFunc = link(TribeCurrentDataQueryDispatcher::perform)
}

interface TribeCurrentDataQueryDispatcher : TribeIdLoadMostSyntax {
    suspend fun perform(query: TribeCurrentDataQuery) = query.tribeId.loadMost()
}