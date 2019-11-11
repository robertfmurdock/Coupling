package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdHistorySyntax
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
    TribeIdHistorySyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { tribeId.getData() }

    private suspend fun TribeId.getData() = with(GlobalScope) {
        Triple(async { load() }, async { loadPlayers() }, async { getHistory() })
            .await()
    }

    private suspend fun Triple<Deferred<KtTribe?>, Deferred<List<Player>>, Deferred<List<PairAssignmentDocument>>>.await() =
        Triple(
            first.await(),
            second.await(),
            third.await()
        )
}