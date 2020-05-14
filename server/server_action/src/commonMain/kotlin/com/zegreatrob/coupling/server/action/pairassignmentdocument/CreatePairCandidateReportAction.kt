package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuccessfulExecutableAction
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player

data class CreatePairCandidateReportAction(
    val player: Player,
    val history: List<PairAssignmentDocument>,
    val allPlayers: List<Player>
) : SimpleSuccessfulExecutableAction<CreatePairCandidateReportActionDispatcher, PairCandidateReport> {
    override val performFunc = link(CreatePairCandidateReportActionDispatcher::perform)
}

interface CreatePairCandidateReportActionDispatcher : PairingTimeCalculationSyntax {

    fun perform(action: CreatePairCandidateReportAction) = action.pairTimeMap()
        .candidateReport()

    private fun CreatePairCandidateReportAction.pairTimeMap() = PairTimeMap(player, timeToPartnersMap())

    private data class PairTimeMap(val player: Player, val timeToPartners: Map<TimeResult, List<Player>>)

    private fun CreatePairCandidateReportAction.timeToPartnersMap() = allPlayers.groupBy { availablePartner ->
        calculateTimeSinceLastPartnership(pair(availablePartner), history)
    }

    private fun CreatePairCandidateReportAction.pair(availablePartner: Player) =
        CouplingPair.Double(player, availablePartner)

    private fun PairTimeMap.candidateReport() = neverPairedReport() ?: longestTimeReport()

    private fun PairTimeMap.neverPairedReport() = timeToPartners[NeverPaired]?.let {
        PairCandidateReport(player, it, NeverPaired)
    }

    private fun PairTimeMap.longestTimeReport() = timeToPartners.findPartnersWithLongestTime()
        ?.let { (timeResult, partners) -> PairCandidateReport(player, partners, timeResult) }
        ?: PairCandidateReport(
            player, emptyList(),
            NeverPaired
        )

    private fun Map<TimeResult, List<Player>>.findPartnersWithLongestTime() =
        maxBy { (key, _) -> if (key is TimeResultValue) key.time else -1 }
}

data class PairCandidateReport(val player: Player, val partners: List<Player>, val timeResult: TimeResult)
