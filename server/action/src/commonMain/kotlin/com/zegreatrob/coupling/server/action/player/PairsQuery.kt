package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
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
