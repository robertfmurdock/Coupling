package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getHistoryAsync
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlin.js.Promise

data class HistoryQuery(val tribeId: TribeId, val coupling: Coupling) : Action

interface HistoryQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax {
    suspend fun HistoryQuery.perform() = logAsync { coupling.getData(tribeId) }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Pair(getTribeAsync(tribeId), getHistoryAsync(tribeId))
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Promise<List<PairAssignmentDocument>>>.await() =
            Pair(
                    first.await(),
                    second.await()
            )

}