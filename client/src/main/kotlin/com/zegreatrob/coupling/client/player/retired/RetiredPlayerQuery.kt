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

typealias RetiredPlayerData = Triple<Party, List<Player>, Player>

data class RetiredPlayerQuery(val partyId: PartyId, val playerId: String) :
    SimpleSuspendAction<RetiredPlayerQueryDispatcher, RetiredPlayerData?> {
    override val performFunc = link(RetiredPlayerQueryDispatcher::perform)
}

interface RetiredPlayerQueryDispatcher : PartyIdGetSyntax, PartyRetiredPlayersSyntax {
    suspend fun perform(query: RetiredPlayerQuery) = query.getData()

    private suspend fun RetiredPlayerQuery.getData() = partyId.getData().let { (party, players) ->
        if (party == null)
            null
        else
            Triple(party, players, players.first { it.id == playerId })
    }

    private suspend fun PartyId.getData() = coroutineScope {
        await(async { get() }, async { loadRetiredPlayers() })
    }
}
