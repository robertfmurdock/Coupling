package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.partyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.PlayerId
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.model.player.matches
import com.zegreatrob.coupling.model.player.pairCombinations
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.coupling.repository.player.PartyIdRetiredPlayerRecordsTrait
import com.zegreatrob.coupling.repository.player.PlayerGetRepository
import com.zegreatrob.coupling.server.action.contribution.PartyIdContributionsTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotools.types.text.toNotBlankString
import org.kotools.types.ExperimentalKotoolsTypesApi

@ActionMint
data class PairListQuery(val partyId: PartyId) {
    interface Dispatcher :
        PartyIdLoadPlayersTrait,
        PartyIdRetiredPlayerRecordsTrait,
        PartyIdContributionsTrait {
        override val playerRepository: PlayerGetRepository

        suspend fun perform(query: PairListQuery): List<PartyElement<PlayerPair>> {
            val (contributions, playerListData) = loadData(query.partyId)

            val naturalPairCombinations = playerListData
                .pairCombinations()

            val allContributionPairs: Set<Set<Record<PartyElement<Player>>>> =
                contributions.mapNotNull { contribution ->
                    contribution.participantEmails
                        .map { email ->
                            playerListData.find { it.data.player.matches(email) }
                                ?: placeholderPlayer(query, email)
                        }
                        .toSet().ifEmpty { null }
                }.toSet()

            val naturalPlayerSets: Set<Set<Record<PartyElement<Player>>>> =
                naturalPairCombinations.mapNotNull { it.players?.toSet() }.toSet()
            val extraPairs = allContributionPairs - naturalPlayerSets

            return query.partyId.with(
                naturalPairCombinations + extraPairs.map {
                    PlayerPair(players = it.toList())
                },
            )
        }

        private suspend fun loadData(partyId: PartyId): Pair<List<Contribution>, List<PartyRecord<Player>>> = coroutineScope {
            val contributions = async { partyId.contributions().elements }
            val playerListData = async { partyId.loadPlayers() }
            val retiredPlayerListData = async { partyId.loadRetiredPlayerRecords() }

            Pair(contributions.await(), playerListData.await() + retiredPlayerListData.await())
        }

        @OptIn(ExperimentalKotoolsTypesApi::class)
        private fun placeholderPlayer(
            query: PairListQuery,
            email: String,
        ) = partyRecord(
            partyId = query.partyId,
            defaultPlayer.copy(id = PlayerId(email.toNotBlankString().getOrThrow()), email = email),
            "-".toNotBlankString().getOrThrow(),
        )
    }
}
