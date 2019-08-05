package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class HistoryQuery(val tribeId: TribeId) : Action

interface HistoryQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPairAssignmentListSyntax {
    suspend fun HistoryQuery.perform() = logAsync { getData(tribeId) }

    private suspend fun getData(tribeId: TribeId) =
            Pair(tribeId.getTribeAsync(), getPairAssignmentListAsync(tribeId))
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<PairAssignmentDocument>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )
}