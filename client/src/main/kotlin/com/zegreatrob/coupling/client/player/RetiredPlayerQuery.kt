package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.Coupling
import com.zegreatrob.coupling.client.getRetiredPlayerListAsync
import com.zegreatrob.coupling.client.getTribeAsync
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.await
import kotlin.js.Promise

data class RetiredPlayerQuery(val tribeId: TribeId, val playerId: String, val coupling: Coupling) : Action

interface RetiredPlayerQueryDispatcher : ActionLoggingSyntax {
    suspend fun RetiredPlayerQuery.perform() = logAsync {
        coupling.getData(tribeId)
                .let { (tribe, players) ->
                    Triple(tribe, players, players.first { it.id == playerId })
                }
    }

    private suspend fun Coupling.getData(tribeId: TribeId) =
            (getTribeAsync(tribeId) to getRetiredPlayerListAsync(tribeId))
                    .await()

    private suspend fun Pair<Promise<KtTribe>, Promise<List<Player>>>.await() =
            first.await() to second.await()
}