package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.tribe.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import kotlinx.coroutines.Deferred

data class RetiredPlayerQuery(val tribeId: TribeId, val playerId: String) : Action

interface RetiredPlayerQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetRetiredPlayerListSyntax {
    suspend fun RetiredPlayerQuery.perform() = logAsync {
        tribeId.getData()
                .let { (tribe, players) ->
                    Triple(tribe, players, players.first { it.id == playerId })
                }
    }

    private suspend fun TribeId.getData() =
            (getTribeAsync() to getRetiredPlayerListAsync())
                    .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Player>>>.await() =
            first.await() to second.await()
}