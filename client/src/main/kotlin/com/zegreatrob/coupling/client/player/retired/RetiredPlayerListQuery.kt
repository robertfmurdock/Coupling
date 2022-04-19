package com.zegreatrob.coupling.client.player.retired

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.coupling.repository.party.PartyIdGetSyntax
import com.zegreatrob.coupling.repository.player.PartyRetiredPlayersSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PlayerListData = Pair<Party, List<Player>>

data class RetiredPlayerListQuery(val partyId: PartyId) :
    SimpleSuspendAction<RetiredPlayerListQueryDispatcher, PlayerListData?> {
    override val performFunc = link(RetiredPlayerListQueryDispatcher::perform)
}

interface RetiredPlayerListQueryDispatcher : PartyIdGetSyntax, PartyRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerListQuery) = getData(query.partyId)

    private suspend fun getData(partyId: PartyId) = coroutineScope {
        await(
            async { partyId.get() },
            async { partyId.loadRetiredPlayers() }
        )
    }.let { (party, players) -> if (party == null) null else party to players }
}
