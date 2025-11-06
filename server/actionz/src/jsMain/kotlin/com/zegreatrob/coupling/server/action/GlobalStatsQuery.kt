package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.stats.medianSpinDuration
import com.zegreatrob.coupling.model.GlobalStats
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PartyStats
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.flatMap
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedPlayer
import com.zegreatrob.coupling.model.pairassignmentdocument.players
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.party.PartyRepository
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotools.types.collection.NotEmptyList
import kotlin.time.Duration.Companion.minutes

@ActionMint
data class GlobalStatsQuery(val year: Int) {

    interface Dispatcher {

        val partyRepository: PartyRepository
        val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository

        suspend fun perform(query: GlobalStatsQuery): GlobalStats = partyRepository.loadParties(emptySet())
            .toStats(yearMatcher(query.year))
            .filter(::excludePartiesSpinningUnnaturallyFast)
            .toGlobalStats()

        private suspend fun List<Record<PartyDetails>>.toStats(
            matchesYear: (PartyRecord<PairAssignmentDocument>) -> Boolean,
        ): List<PartyStats> = asFlow()
            .map { it to pairAssignmentDocumentRepository.loadPairAssignments(it.data.id) }
            .filter { (_, docs) -> docs.any(matchesYear) }
            .map { (party, docs) -> partyStats(party, docs, matchesYear) }
            .toList()

        private fun excludePartiesSpinningUnnaturallyFast(stats: PartyStats): Boolean {
            val medianSpinDuration = stats.medianSpinDuration
            return medianSpinDuration != null && medianSpinDuration > 1.minutes
        }

        private fun List<PartyStats>.toGlobalStats() = GlobalStats(
            parties = this,
            totalParties = size,
            totalSpins = sumOf(PartyStats::spins),
            totalPlayers = sumOf(PartyStats::playerCount),
            totalAppliedPins = sumOf(PartyStats::appliedPinCount),
            totalUniquePins = sumOf(PartyStats::uniquePinCount),
        )
    }
}

private fun yearMatcher(year: Int) = { record: PartyRecord<PairAssignmentDocument> ->
    record.data.element.date.toLocalDateTime(TimeZone.currentSystemDefault()).year == year
}

private fun partyStats(
    party: Record<PartyDetails>,
    docs: List<PartyRecord<PairAssignmentDocument>>,
    filter: (PartyRecord<PairAssignmentDocument>) -> Boolean,
): PartyStats {
    val pairDocsThisYear = docs.filter(filter).elements
    return PartyStats(
        name = party.data.name ?: party.data.id.value.toString(),
        id = party.data.id,
        playerCount = pairDocsThisYear.distinctPlayersPairedThisYear().size,
        spins = pairDocsThisYear.size,
        medianSpinDuration = pairDocsThisYear.medianSpinDuration(),
        appliedPinCount = pairDocsThisYear.allPins().size,
        uniquePinCount = pairDocsThisYear.allPins().map(Pin::id).distinct().size,
    )
}

private fun List<PairAssignmentDocument>.allPins(): List<Pin> = flatMap {
    it.pairs.toList().flatMap(PinnedCouplingPair::allPins)
}

private fun PinnedCouplingPair.allPins(): List<Pin> = pins.toList()
    .plus(pinnedPlayers.flatMap(PinnedPlayer::pins))

private fun List<PairAssignmentDocument>.distinctPlayersPairedThisYear() = map(PairAssignmentDocument::pairs)
    .flatMap(NotEmptyList<PinnedCouplingPair>::toList)
    .asSequence()
    .map(PinnedCouplingPair::players)
    .map(NotEmptyList<Player>::toList)
    .flatten()
    .map(Player::id)
    .distinct()
    .toList()
