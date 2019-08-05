package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.player.GetPlayerListSyntax
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax, GetPairAssignmentListSyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { getData(tribeId) }

    private suspend fun getData(tribeId: TribeId) =
            Triple(tribeId.getTribeAsync(), getPlayerListAsync(tribeId), getPairAssignmentListAsync(tribeId))
                    .await()

    private suspend fun Triple<Deferred<KtTribe>, Deferred<List<Player>>, Deferred<List<PairAssignmentDocument>>>.await() =
            Triple(
                    first.await(),
                    second.await(),
                    third.await())
}