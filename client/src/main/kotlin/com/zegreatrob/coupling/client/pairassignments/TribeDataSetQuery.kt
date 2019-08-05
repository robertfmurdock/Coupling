package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getHistoryAsync
import com.zegreatrob.coupling.client.getPlayerListAsync
import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.await
import kotlin.js.Promise

data class TribeDataSetQuery(val tribeId: TribeId, val coupling: Coupling) : Action

interface TribeDataSetQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax {

    suspend fun TribeDataSetQuery.perform() = logAsync { coupling.getData(tribeId) }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            Triple(getTribeAsync(tribeId), getPlayerListAsync(tribeId), getHistoryAsync(tribeId))
                    .await()

    private suspend fun Triple<Deferred<KtTribe>, Promise<List<Player>>, Promise<List<PairAssignmentDocument>>>.await() =
            Triple(
                    first.await(),
                    second.await(),
                    third.await())
}