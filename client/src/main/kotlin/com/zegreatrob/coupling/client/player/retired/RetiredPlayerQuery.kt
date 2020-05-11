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

data class RetiredPlayerQuery(val tribeId: TribeId, val playerId: String) :
    SuspendAction<RetiredPlayerQueryDispatcher, Triple<Tribe?, List<Player>, Player>> {
    override suspend fun execute(dispatcher: RetiredPlayerQueryDispatcher) = with(dispatcher) { perform() }
}

interface RetiredPlayerQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun RetiredPlayerQuery.perform() = tribeId.getData()
        .let { (tribe, players) ->
            Triple(tribe, players, players.first { it.id == playerId })
        }.successResult()

    private suspend fun TribeId.getData() = coroutineScope {
        await(async { get() }, async { loadRetiredPlayers() })
    }
}