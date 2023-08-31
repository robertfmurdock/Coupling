package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.pairCombinations
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairsQuery(val partyId: PartyId) {
    interface Dispatcher {
        suspend fun perform(query: PairsQuery): List<PartyElement<PlayerPair>>
    }
}

interface ServerPairsQueryDispatcher : PairsQuery.Dispatcher, PartyIdLoadPlayersTrait {
    override suspend fun perform(query: PairsQuery): List<PartyElement<PlayerPair>> = query.partyId.with(
        query.partyId.loadPlayers()
            .pairCombinations(),
    )
}

@ActionMint
data class PairQuery(val partyId: PartyId, val playerIds: List<String>) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        suspend fun perform(query: PairQuery): PartyElement<PlayerPair>? =
            query.partyId.loadPlayers()
                .pairCombinations()
                .firstOrNull { it.players?.elements?.map(Player::id) == query.playerIds }
                ?.let { query.partyId.with(it) }
    }
}
