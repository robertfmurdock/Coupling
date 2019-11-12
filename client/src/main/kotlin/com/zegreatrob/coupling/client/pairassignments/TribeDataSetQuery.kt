package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.client.stats.TribeIdLoadAllSyntax
import com.zegreatrob.coupling.model.tribe.TribeId

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, TribeIdLoadAllSyntax {
    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.loadAll() }
}
