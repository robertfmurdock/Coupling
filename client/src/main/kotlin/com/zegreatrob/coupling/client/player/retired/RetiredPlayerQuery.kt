package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class RetiredPlayerQuery(val tribeId: TribeId, val playerId: String) :
    SimpleSuspendAction<RetiredPlayerQueryDispatcher, Triple<Tribe?, List<Player>, Player>> {
    override val performFunc = link(RetiredPlayerQueryDispatcher::perform)
}

interface RetiredPlayerQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerQuery) = query.getData().successResult()

    private suspend fun RetiredPlayerQuery.getData() = tribeId.getData()
        .let { (tribe, players) ->
            Triple(tribe, players, players.first { it.id == playerId })
        }

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { loadRetiredPlayers() })
    }
}