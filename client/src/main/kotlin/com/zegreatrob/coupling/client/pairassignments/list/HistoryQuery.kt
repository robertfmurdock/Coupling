package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.client.sdk.GetPairAssignmentListSyntax
import com.zegreatrob.coupling.client.sdk.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class HistoryQuery(val tribeId: TribeId) : Action

interface HistoryQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPairAssignmentListSyntax {
    suspend fun HistoryQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
        Pair(getTribeAsync(), getPairAssignmentListAsync())
            .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<PairAssignmentDocument>>>.await() =
        Pair(
            first.await(),
            second.await()
        )
}