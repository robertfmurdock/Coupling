package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.pairCombinations
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairQuery(val partyId: PartyId, val playerIds: Set<PlayerId>) {
    interface Dispatcher : PartyIdLoadPlayersTrait {
        suspend fun perform(query: PairQuery): PartyElement<PlayerPair>? = query.partyId.loadPlayers()
            .pairCombinations()
            .firstOrNull { it.players?.elements?.map(Player::id)?.toSet() == query.playerIds }
            ?.let { query.partyId.with(it) }
    }
}
