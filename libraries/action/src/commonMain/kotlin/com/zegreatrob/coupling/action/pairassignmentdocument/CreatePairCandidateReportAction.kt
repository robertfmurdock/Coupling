package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.coupling.model.pairassignmentdocument.calculateTimeSinceLastPartnership
import com.zegreatrob.coupling.model.pairassignmentdocument.pairOf
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class CreatePairCandidateReportAction(
    val player: Player,
    val history: List<PairAssignmentDocument>,
    val allPlayers: List<Player>,
) {

    interface Dispatcher {

        fun perform(action: CreatePairCandidateReportAction) = action.pairTimeMap()
            .candidateReport()

        private fun CreatePairCandidateReportAction.pairTimeMap() = PairTimeMap(player, timeToPartnersMap())

        private data class PairTimeMap(val player: Player, val timeToPartners: Map<TimeResult, List<Player>>)

        private fun CreatePairCandidateReportAction.timeToPartnersMap() = allPlayers.groupBy { availablePartner ->
            calculateTimeSinceLastPartnership(pair(availablePartner), history)
        }

        private fun CreatePairCandidateReportAction.pair(availablePartner: Player) = pairOf(player, availablePartner)

        private fun PairTimeMap.candidateReport() = neverPairedReport() ?: longestTimeReport()

        private fun PairTimeMap.neverPairedReport() = timeToPartners[NeverPaired]?.let {
            PairCandidateReport(player, it, NeverPaired)
        }

        private fun PairTimeMap.longestTimeReport() = timeToPartners.findPartnersWithLongestTime()
            ?.let { (timeResult, partners) -> PairCandidateReport(player, partners, timeResult) }
            ?: PairCandidateReport(player, emptyList(), NeverPaired)

        private fun Map<TimeResult, List<Player>>.findPartnersWithLongestTime() = maxByOrNull { (key, _) -> if (key is TimeResultValue) key.time else -1 }
    }
}
