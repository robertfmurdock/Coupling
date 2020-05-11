package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class RetiredPlayerListQuery(val tribeId: TribeId) :
    SuspendAction<RetiredPlayerListQueryDispatcher, Pair<Tribe?, List<Player>>> {
    override suspend fun execute(dispatcher: RetiredPlayerListQueryDispatcher) = with(dispatcher) { perform() }
}

interface RetiredPlayerListQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayerListQuery.perform() = getData(tribeId).successResult()
    private suspend fun getData(tribeId: TribeId) = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.loadRetiredPlayers() })
    }
}