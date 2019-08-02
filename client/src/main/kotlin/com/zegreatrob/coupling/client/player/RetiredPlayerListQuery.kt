package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getRetiredPlayerListAsync
import com.zegreatrob.coupling.client.getTribeAsync
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import kotlin.js.Promise

data class RetiredPlayerListQuery(val tribeId: TribeId, val coupling: Coupling)

interface RetiredPlayerListQueryDispatcher {
    suspend fun RetiredPlayerListQuery.perform() = coupling.getData(tribeId)

    private suspend fun Coupling.getData(tribeId: TribeId): Pair<KtTribe, List<Player>> =
            (getTribeAsync(tribeId) to getRetiredPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() = first.await() to second.await()
}