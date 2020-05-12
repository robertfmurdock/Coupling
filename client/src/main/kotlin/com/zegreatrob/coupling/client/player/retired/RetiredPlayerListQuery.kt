package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.action.SimpleSuspendAction
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
    SimpleSuspendAction<RetiredPlayerListQuery, RetiredPlayerListQueryDispatcher, Pair<Tribe?, List<Player>>> {
    override val perform = link(RetiredPlayerListQueryDispatcher::perform)
}

interface RetiredPlayerListQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerListQuery) = getData(query.tribeId).successResult()
    private suspend fun getData(tribeId: TribeId) = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.loadRetiredPlayers() })
    }
}