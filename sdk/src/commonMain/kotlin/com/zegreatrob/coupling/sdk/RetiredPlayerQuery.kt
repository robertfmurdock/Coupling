package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

typealias RetiredPlayerData = Triple<Party, List<Player>, Player>

data class RetiredPlayerQuery(val partyId: PartyId, val playerId: String) :
    SimpleSuspendAction<RetiredPlayerQuery.Dispatcher, RetiredPlayerData?> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(query: RetiredPlayerQuery): Triple<Party, List<Player>, Player>?
    }
}

interface ClientRetiredPlayerQueryDispatcher :
    SdkProviderSyntax,
    RetiredPlayerQuery.Dispatcher {
    override suspend fun perform(query: RetiredPlayerQuery) = query.getData()

    private suspend fun RetiredPlayerQuery.getData() = partyId.getData()?.let { (party, players) ->
        if (party == null) {
            null
        } else {
            Triple(party, players, players.first { it.id == playerId })
        }
    }

    private suspend fun PartyId.getData() = sdk.perform(
        graphQuery {
            party(this@getData) {
                party()
                retiredPlayers()
            }
        },
    )?.partyData?.let { it.party?.data to (it.retiredPlayers?.elements ?: emptyList()) }
}
