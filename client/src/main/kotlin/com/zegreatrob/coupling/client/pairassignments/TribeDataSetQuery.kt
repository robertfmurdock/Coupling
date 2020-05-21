package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.client.stats.TribeData
import com.zegreatrob.coupling.client.stats.TribeIdLoadAllSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class TribeDataSetQuery(val tribeId: TribeId) :
    SimpleSuspendAction<TribeDataSetQueryDispatcher, TribeData> {
    override val performFunc = link(TribeDataSetQueryDispatcher::perform)
}

interface TribeDataSetQueryDispatcher : TribeIdLoadAllSyntax {
    suspend fun perform(query: TribeDataSetQuery) = query.tribeId.loadAll().successResult()
}
