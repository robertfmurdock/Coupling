package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.client.stats.TribeData
import com.zegreatrob.coupling.client.stats.TribeIdLoadAllSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class TribeDataSetQuery(val tribeId: TribeId) : SuspendAction<TribeDataSetQueryDispatcher, TribeData> {
    override suspend fun execute(dispatcher: TribeDataSetQueryDispatcher) = with(dispatcher) { perform() }
}

interface TribeDataSetQueryDispatcher : TribeIdLoadAllSyntax {
    suspend fun TribeDataSetQuery.perform() = tribeId.loadAll().successResult()
}
