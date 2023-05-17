package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.await
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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

    private suspend fun RetiredPlayerQuery.getData() = partyId.getData().let { (party, players) ->
        if (party == null) {
            null
        } else {
            Triple(party, players, players.first { it.id == playerId })
        }
    }

    private suspend fun PartyId.getData() = coroutineScope {
        await(
            async {
                sdk.perform(graphQuery { party(this@getData) { party() } })
                    ?.partyData
                    ?.party?.data
            },
            async { sdk.getDeleted(this@getData).map { it.data.element } },
        )
    }
}
