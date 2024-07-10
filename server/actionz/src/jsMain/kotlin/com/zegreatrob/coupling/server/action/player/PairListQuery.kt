package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.player.pairCombinations
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.coupling.server.action.contribution.PartyIdContributionsTrait
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PairListQuery(val partyId: PartyId) {
    interface Dispatcher :
        PartyIdLoadPlayersTrait,
        PartyIdContributionsTrait {
        suspend fun perform(query: PairListQuery): List<PartyElement<PlayerPair>> {
            val contributions = query.partyId.contributions().elements
            val playerListData = query.partyId.loadPlayers()

            val naturalPairCombinations = playerListData
                .pairCombinations()

            val allContributionPairs = contributions.mapNotNull { contribution ->
                contribution.participantEmails
                    .mapNotNull { email -> playerListData.find { it.data.player.email == email } }
                    .toSet().ifEmpty { null }
            }.toSet()

            val extraPairs = allContributionPairs - naturalPairCombinations.mapNotNull { it.players }.toSet()

            return query.partyId.with(
                naturalPairCombinations + extraPairs.map { PlayerPair(players = it.toList()) },
            )
        }
    }
}
