package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistorySyntax
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList
import kotlin.math.min

const val rotationHeatWindow = 5
val heatIncrements = listOf(0.0, 1.0, 2.5, 4.5, 7.0, 10.0)

@ActionMint
data class PairHeatQuery(
    val partyId: PartyId,
    val pair: CouplingPair,
    val rotationPeriod: Int,
) {
    interface Dispatcher : PartyIdHistorySyntax {
        suspend fun perform(action: PairHeatQuery) = action.timesPaired()
            .toHeatIncrement()

        private suspend fun PairHeatQuery.timesPaired() = historyInHeatWindow()
            .flattenedPairings()
            .count { equivalent(it, pair) }

        private suspend fun PairHeatQuery.historyInHeatWindow(): List<PairAssignmentDocument> {
            val history = partyId.loadHistory()
            return history.slice(
                0 until min(lastRelevantRotation, history.size),
            )
        }

        private val PairHeatQuery.lastRelevantRotation get() = rotationPeriod * rotationHeatWindow

        private fun List<PairAssignmentDocument>.flattenedPairings() = map(PairAssignmentDocument::pairs)
            .map(NotEmptyList<PinnedCouplingPair>::toList)
            .flatten()
            .map(PinnedCouplingPair::toPair)

        private fun Int.toHeatIncrement() = heatIncrements[incrementIndex(this)]

        private fun incrementIndex(timesPaired: Int) = min(timesPaired, heatIncrements.size - 1)
    }
}
