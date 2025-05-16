package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.element
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
data class PairListQuery(val partyId: PartyId, val includeRetired: Boolean?) {
    interface Dispatcher :
        PartyIdLoadPlayersTrait,
        PartyIdRetiredPlayerRecordsTrait,
        PartyIdContributionsTrait {
        override val playerRepository: PlayerGetRepository

        suspend fun perform(query: PairListQuery): List<PartyElement<PlayerPair>> {
            val (contributions, playerListData, retiredPlayerListData) = query.loadData()

            val allPlayerData = playerListData + retiredPlayerListData
            val naturalPairCombinations = allPlayerData
                .pairCombinations()

            val allContributionPairs: Set<Set<Record<PartyElement<Player>>>> =
                contributions.mapNotNull { contribution ->
                    contribution.participantEmails
                        .map { email ->
                            allPlayerData.find { it.data.player.matches(email) }
                                ?: placeholderPlayer(query, email)
                        }
                        .toSet().ifEmpty { null }
                }.toSet()

            val naturalPlayerSets: Set<Set<Record<PartyElement<Player>>>> =
                naturalPairCombinations.mapNotNull { it.players?.toSet() }.toSet()
            val extraPairs = allContributionPairs - naturalPlayerSets

            val filter: (PlayerPair) -> Boolean = if (query.includeRetired == true) {
                ({ true })
            } else {
                ({ pair -> !pair.anyPlayersAreRetired(retiredPlayerListData) })
            }
            return query.partyId.with(
                (
                    naturalPairCombinations + extraPairs.map {
                        PlayerPair(players = it.toList())
                    }
                    ).filter(filter),
            )
        }

        fun PlayerPair.anyPlayersAreRetired(
            retiredPlayerListData: List<PartyRecord<Player>>,
        ): Boolean = players?.map { it.element.id }?.any { playerId ->
            retiredPlayerListData.any { it.data.player.id == playerId }
        } == true

        private suspend fun PairListQuery.loadData() = coroutineScope {
            val contributions = async { partyId.contributions().elements }
            val playerListData = async { partyId.loadPlayers() }
            val retiredPlayerListData = async { partyId.loadRetiredPlayerRecords() }

            Triple(contributions.await(), playerListData.await(), retiredPlayerListData.await())
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
