package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.stats.spinsUntilFullRotation
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistoryTrait
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList
import kotlin.math.min

const val rotationHeatWindow = 5
val heatIncrements = listOf(0.0, 1.0, 2.5, 4.5, 7.0, 10.0)

@ActionMint
data class PairHeatQuery(val partyId: PartyId, val pair: CouplingPair, val lastAssignments: PairAssignmentDocumentId?) {
    interface Dispatcher : PartyIdHistoryTrait, PartyIdLoadPlayersTrait {
        suspend fun perform(action: PairHeatQuery) = action.timesPaired()
            ?.toHeatIncrement()

        private suspend fun PairHeatQuery.timesPaired() = when (pair) {
            is CouplingPair.Single -> null
            is CouplingPair.Double -> historyInHeatWindow()
                .flattenedPairings()
                .count { equivalent(it, pair) }
        }

        private suspend fun PairHeatQuery.historyInHeatWindow(): List<PairAssignmentDocument> {
            val history = partyId.loadHistory()
                .sortedBy { it.date }
                .limitHistory(lastAssignments)
            val rotationPeriod = partyId.loadPlayers().elements.spinsUntilFullRotation()
            return history.slice(
                0 until min(getLastRelevantRotation(rotationPeriod), history.size),
            )
        }

        private fun List<PairAssignmentDocument>.limitHistory(pairAssignmentDocumentId: PairAssignmentDocumentId?) =
            if (pairAssignmentDocumentId != null) {
                slice(0..map(PairAssignmentDocument::id).indexOf(pairAssignmentDocumentId).also { println("index is $it") })
            } else {
                this
            }

        private fun getLastRelevantRotation(rotationPeriod: Int) = rotationPeriod * rotationHeatWindow

        private fun List<PairAssignmentDocument>.flattenedPairings() = map(PairAssignmentDocument::pairs)
            .map(NotEmptyList<PinnedCouplingPair>::toList)
            .flatten()
            .map(PinnedCouplingPair::toPair)

        private fun Int.toHeatIncrement() = heatIncrements[incrementIndex(this)]

        private fun incrementIndex(timesPaired: Int) = min(timesPaired, heatIncrements.size - 1)
    }
}
