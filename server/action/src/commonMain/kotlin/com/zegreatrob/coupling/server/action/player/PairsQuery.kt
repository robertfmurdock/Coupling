package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersSyntax
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairsQuery(val partyId: PartyId) {
    interface Dispatcher {
        suspend fun perform(query: PairsQuery): List<PartyElement<PlayerPair>>
    }
}

interface ServerPairsQueryDispatcher : PairsQuery.Dispatcher, PartyIdLoadPlayersSyntax {
    override suspend fun perform(query: PairsQuery): List<PartyElement<PlayerPair>> = query.partyId.with(
        query.partyId.loadPlayers()
            .allPairCombinations(),
    )

    private fun List<PartyRecord<Player>>.allPairCombinations() = mapIndexed { index, player ->
        slice(index + 1..lastIndex).toPairsWith(player)
    }.flatten()

    private fun List<PartyRecord<Player>>.toPairsWith(player: PartyRecord<Player>) = map {
        PlayerPair(listOf(player, it))
    }
}
