package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.client.sdk.GetPairAssignmentListSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayersSyntax
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

data class TribeDataSetQuery(val tribeId: TribeId) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdPlayersSyntax,
    GetPairAssignmentListSyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() =
        Triple(loadAsync(), GlobalScope.async { loadPlayers() }, getPairAssignmentListAsync())
            .await()

    private suspend fun Triple<Deferred<KtTribe?>, Deferred<List<Player>>, Deferred<List<PairAssignmentDocument>>>.await() =
        Triple(
            first.await(),
            second.await(),
            third.await()
        )
}