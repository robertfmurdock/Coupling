package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
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

            val allContributionPairs: Set<Set<Record<PartyElement<Player>>>> =
                contributions.mapNotNull { contribution ->
                    contribution.participantEmails
                        .map { email ->
                            playerListData.find { it.data.player.email == email } ?: placeholderPlayer(query, email)
                        }
                        .toSet().ifEmpty { null }
                }.toSet()

            val naturalPlayerSets: Set<Set<Record<PartyElement<Player>>>> =
                naturalPairCombinations.mapNotNull { it.players?.toSet() }.toSet()
            val extraPairs = allContributionPairs - naturalPlayerSets

            return query.partyId.with(
                naturalPairCombinations + extraPairs.map { PlayerPair(players = it.toList()) },
            )
        }

        private fun placeholderPlayer(
            query: PairListQuery,
            email: String,
        ) = partyRecord(
            partyId = query.partyId,
            defaultPlayer.copy(id = email, email = email),
            "",
        )
    }
}
