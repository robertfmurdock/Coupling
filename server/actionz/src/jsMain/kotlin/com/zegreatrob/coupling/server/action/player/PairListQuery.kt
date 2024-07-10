package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.pairCombinations
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairListQuery(val partyId: PartyId) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        suspend fun perform(query: PairListQuery): List<PartyElement<PlayerPair>> = query.partyId.with(
            query.partyId.loadPlayers()
                .pairCombinations(),
        )
    }
}
