package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.client.sdk.GetRetiredPlayerListSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import kotlinx.coroutines.Deferred

data class RetiredPlayerListQuery(val tribeId: TribeId) : Action

interface RetiredPlayerListQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, GetRetiredPlayerListSyntax {
    suspend fun RetiredPlayerListQuery.perform() = logAsync { getData(tribeId) }

    private suspend fun getData(tribeId: TribeId) =
        (tribeId.loadAsync() to tribeId.getRetiredPlayerListAsync())
            .await()

    private suspend fun Pair<Deferred<KtTribe?>, Deferred<List<Player>>>.await() = first.await() to second.await()
}