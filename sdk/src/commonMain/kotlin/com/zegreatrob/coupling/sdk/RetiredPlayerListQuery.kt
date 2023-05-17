package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

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

    private suspend fun getData(partyId: PartyId) = sdk.perform(
        graphQuery {
            party(partyId) {
                party()
                retiredPlayers()
            }
        },
    )?.partyData
        ?.let { it.party?.data to it.retiredPlayers?.elements }
        ?.let { (party, players) -> if (party == null || players == null) null else party to players }
}
