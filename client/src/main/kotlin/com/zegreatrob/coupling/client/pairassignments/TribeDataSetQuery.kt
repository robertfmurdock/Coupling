package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.client.stats.TribeData
import com.zegreatrob.coupling.client.stats.TribeIdLoadAllSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class TribeDataSetQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribeDataSetQuery, TribeDataSetQueryDispatcher, TribeData> {
    override val perform = link(TribeDataSetQueryDispatcher::perform)
}

interface TribeDataSetQueryDispatcher : TribeIdLoadAllSyntax {
    suspend fun perform(query: TribeDataSetQuery) = query.tribeId.loadAll().successResult()
}
