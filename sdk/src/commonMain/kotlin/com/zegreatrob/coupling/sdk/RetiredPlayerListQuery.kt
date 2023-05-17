package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

typealias PlayerListData = Pair<Party, List<Player>>

data class RetiredPlayerListQuery(val partyId: PartyId) :
    SimpleSuspendAction<RetiredPlayerListQuery.Dispatcher, PlayerListData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: RetiredPlayerListQuery): Pair<Party, List<Player>>?
    }
}

interface ClientRetiredPlayerListQueryDispatcher :
    SdkProviderSyntax,
    RetiredPlayerListQuery.Dispatcher {
    override suspend fun perform(query: RetiredPlayerListQuery) = getData(query.partyId)

    private suspend fun getData(partyId: PartyId) = coroutineScope {
        await(
            async {
                sdk.perform(graphQuery { party(partyId) { party() } })
                    ?.partyData
                    ?.party?.data
            },
            async {
                sdk.perform(graphQuery { party(partyId) { retiredPlayers() } })
                    ?.partyData
                    ?.retiredPlayers
                    .let { it ?: emptyList() }.map { it.data.element }
            },
        )
    }.let { (party, players) -> if (party == null) null else party to players }
}
