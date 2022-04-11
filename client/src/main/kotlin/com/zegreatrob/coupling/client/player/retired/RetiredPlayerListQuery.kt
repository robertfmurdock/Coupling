package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Party
import com.zegreatrob.coupling.model.tribe.PartyId
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.player.TribeIdRetiredPlayersSyntax
import com.zegreatrob.coupling.repository.tribe.TribeIdGetSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PlayerListData = Pair<Party, List<Player>>

data class RetiredPlayerListQuery(val tribeId: PartyId) :
    SimpleSuspendAction<RetiredPlayerListQueryDispatcher, PlayerListData?> {
    override val performFunc = link(RetiredPlayerListQueryDispatcher::perform)
}

interface RetiredPlayerListQueryDispatcher : TribeIdGetSyntax, TribeIdRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerListQuery) = getData(query.tribeId)

    private suspend fun getData(tribeId: PartyId) = coroutineScope {
        await(
            async { tribeId.get() },
            async { tribeId.loadRetiredPlayers() })
    }.let { (tribe, players) -> if (tribe == null) null else tribe to players }
}