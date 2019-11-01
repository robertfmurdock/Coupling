package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.client.sdk.GetRetiredPlayerListSyntax
import com.zegreatrob.coupling.client.sdk.GetTribeSyntax
import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import kotlinx.coroutines.Deferred

data class RetiredPlayerListQuery(val tribeId: TribeId) : Action

interface RetiredPlayerListQueryDispatcher : ActionLoggingSyntax, GetTribeSyntax, GetRetiredPlayerListSyntax {
    suspend fun RetiredPlayerListQuery.perform() = logAsync { getData(tribeId) }

    private suspend fun getData(tribeId: TribeId) =
        (tribeId.getTribeAsync() to tribeId.getRetiredPlayerListAsync())
            .await()

    private suspend fun Pair<Deferred<KtTribe>, Deferred<List<Player>>>.await() = first.await() to second.await()
}