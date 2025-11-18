package com.zegreatrob.coupling.server.action.player

import com.zegreatrob.coupling.action.stats.spinsUntilFullRotation
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair.Companion.equivalent
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSetId
import com.zegreatrob.coupling.model.pairassignmentdocument.PinnedCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdHistoryTrait
import com.zegreatrob.coupling.repository.player.PartyIdLoadPlayersTrait
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList
import kotlin.math.min

const val ROTATION_HEAT_WINDOW = 5

@ActionMint
data class RecentTimesPairedQuery(val partyId: PartyId, val pair: CouplingPair, val lastAssignments: PairingSetId?) {
    interface Dispatcher :
        PartyIdHistoryTrait,
        PartyIdLoadPlayersTrait {
        suspend fun perform(action: RecentTimesPairedQuery) = action.timesPaired()

        private suspend fun RecentTimesPairedQuery.timesPaired() = when (pair) {
            is CouplingPair.Single -> null

            is CouplingPair.Double -> historyInHeatWindow()
                .flattenedPairings()
                .count { equivalent(it, pair) }

            is CouplingPair.Mob -> null
        }

        private suspend fun RecentTimesPairedQuery.historyInHeatWindow(): List<PairingSet> {
            val history = partyId.loadHistory()
                .sortedBy { it.date }
                .limitHistory(lastAssignments)
            val rotationPeriod = partyId.loadPlayers().elements.spinsUntilFullRotation()
            return history.reversed()
                .slice(0 until min(getLastRelevantRotation(rotationPeriod), history.size))
        }

        private fun List<PairingSet>.limitHistory(pairingSetId: PairingSetId?) = if (pairingSetId != null) {
            slice(0..map(PairingSet::id).indexOf(pairingSetId))
        } else {
            this
        }

        private fun getLastRelevantRotation(rotationPeriod: Int) = rotationPeriod * ROTATION_HEAT_WINDOW

        private fun List<PairingSet>.flattenedPairings() = map(PairingSet::pairs)
            .map(NotEmptyList<PinnedCouplingPair>::toList)
            .flatten()
            .map(PinnedCouplingPair::toPair)
    }
}
