package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.sdk.GetPlayerListSyntax
import com.zegreatrob.coupling.client.sdk.GetPairAssignmentListSyntax
import com.zegreatrob.coupling.client.sdk.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetPlayerListSyntax,
    GetPairAssignmentListSyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
        Triple(getTribeAsync(), getPlayerListAsync(), getPairAssignmentListAsync())
            .await()

    private suspend fun Triple<Deferred<KtTribe>, Deferred<List<Player>>, Deferred<List<PairAssignmentDocument>>>.await() =
        Triple(
            first.await(),
            second.await(),
            third.await()
        )
}