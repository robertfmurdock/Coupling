package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.action.NotFoundResult
import com.zegreatrob.coupling.action.Result
import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PlayerListData = Pair<Tribe, List<Player>>

data class RetiredPlayerListQuery(val tribeId: TribeId) :
    SimpleSuspendResultAction<RetiredPlayerListQueryDispatcher, PlayerListData> {
    override val performFunc = link(RetiredPlayerListQueryDispatcher::perform)
}

interface RetiredPlayerListQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerListQuery): Result<PlayerListData> = getData(query.tribeId)
        ?.successResult()
        ?: NotFoundResult("tribe")

    private suspend fun getData(tribeId: TribeId) = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.loadRetiredPlayers() })
    }.let { (tribe, players) -> if (tribe == null) null else tribe to players }
}