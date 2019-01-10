import kotlin.js.JsName

interface PairingHistoryReporter : PairingTimeCalculationSyntax {

    @JsName("createPairCandidateReport")
    fun createPairCandidateReport(history: List<HistoryDocument>, player: Player, allPlayers: Array<Player>) =
            CreatePairCandidateReportCommand(history, player, allPlayers.asList())
                    .perform()

    private fun CreatePairCandidateReportCommand.perform() = pairTimeMap()
            .candidateReport()

    private fun CreatePairCandidateReportCommand.pairTimeMap() = PairTimeMap(player, timeToPartnersMap())

    private data class PairTimeMap(val player: Player, val timeToPartners: Map<TimeResult, List<Player>>)

    private fun CreatePairCandidateReportCommand.timeToPartnersMap() = allPlayers.groupBy { availablePartner ->
        calculateTimeSinceLastPartnership(pair(availablePartner), history)
    }

    private fun CreatePairCandidateReportCommand.pair(availablePartner: Player) =
            CouplingPair.Double(player, availablePartner)

    private fun PairTimeMap.candidateReport() = neverPairedReport() ?: longestTimeReport()

    private fun PairTimeMap.neverPairedReport() = timeToPartners[NeverPaired]?.let {
        PairCandidateReport(player, it, NeverPaired)
    }

    private fun PairTimeMap.longestTimeReport() = timeToPartners.findPartnersWithLongestTime()
            ?.let { (timeResult, partners) -> PairCandidateReport(player, partners, timeResult) }
            ?: PairCandidateReport(player, emptyList(), NeverPaired)

    private fun Map<TimeResult, List<Player>>.findPartnersWithLongestTime() =
            maxBy { (key, _) -> if (key is TimeResultValue) key.time else -1 }
}


data class CreatePairCandidateReportCommand(val history: List<HistoryDocument>, val player: Player, val allPlayers: List<Player>)

data class PairCandidateReport(val player: Player, val partners: List<Player>, val timeResult: TimeResult) {

    @JsName("partnersAsArray")
    fun partnersAsArray() = partners.toTypedArray()

}