package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.KtTribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.TribeIdGetSyntax
import com.zegreatrob.coupling.model.player.TribeIdRetiredPlayersSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

data class RetiredPlayerListQuery(val tribeId: TribeId) : Action

interface RetiredPlayerListQueryDispatcher : ActionLoggingSyntax, TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {

    suspend fun RetiredPlayerListQuery.perform() = logAsync { getData(tribeId) }

    private suspend fun getData(tribeId: TribeId) = with(GlobalScope) {
        (async { tribeId.load() } to async { tribeId.loadRetiredPlayers() })
            .await()
    }

    private suspend fun Pair<Deferred<KtTribe?>, Deferred<List<Player>>>.await() = first.await() to second.await()
}