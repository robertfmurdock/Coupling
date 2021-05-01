package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.stats.TribeData
import com.zegreatrob.coupling.client.stats.TribeIdLoadAllSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class TribeDataSetQuery(val tribeId: TribeId) : SimpleSuspendAction<TribeDataSetQueryDispatcher, TribeData?> {
    override val performFunc = link(TribeDataSetQueryDispatcher::perform)
}

interface TribeDataSetQueryDispatcher : TribeIdLoadAllSyntax {
    suspend fun perform(query: TribeDataSetQuery) = query.tribeId.loadAll()
}
