package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdHistorySyntax
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred

data class HistoryQuery(val tribeId: TribeId) : Action

interface HistoryQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdHistorySyntax {
    suspend fun HistoryQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
        Pair(loadAsync(), getHistoryAsync())
            .await()

    private suspend fun Pair<Deferred<KtTribe?>, Deferred<List<PairAssignmentDocument>>>.await() =
        Pair(
            first.await(),
            second.await()
        )
}