import kotlin.js.JsName

data class CreateAllPairCandidateReportsCommand(val history: List<HistoryDocument>, val players: List<Player>, val rule: PairingRule)

interface CreateAllPairCandidateReportsCommandDispatcher : PlayerCandidatesFinder {

    @JsName("actionDispatcher")
    val actionDispatcher: CreatePairCandidateReportActionDispatcher

    private fun CreatePairCandidateReportAction.performThis() = with(actionDispatcher) { perform() }

    @JsName("createAllPairCandidateReport")
    fun createAllPairCandidateReport(history: List<HistoryDocument>, players: Array<Player>, rule: PairingRule) =
            CreateAllPairCandidateReportsCommand(history, players.toList(), rule)
                    .perform()
                    .toTypedArray()

    fun CreateAllPairCandidateReportsCommand.perform(): List<PairCandidateReport> {
        val reportsForGivenRule = rule.getReports(history, players)

        return if (reportsForGivenRule.isEmpty()) {
            PairingRule.LongestTime.getReports(history, players)
        } else {
            reportsForGivenRule
        }
    }

    private fun PairingRule.getReports(history: List<HistoryDocument>, players: List<Player>): List<PairCandidateReport> {
        return players.mapNotNull { player ->
            val candidates = findCandidates(players.toTypedArray(), this, player)
            if (candidates.isNotEmpty() || this == PairingRule.LongestTime) {
                CreatePairCandidateReportAction(history, player, candidates.toList())
                        .performThis()
            } else {
                null
            }
        }
    }
}