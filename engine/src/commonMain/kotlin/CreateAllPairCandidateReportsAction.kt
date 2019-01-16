import kotlin.js.JsName

data class CreateAllPairCandidateReportsAction(val game: GameSpin)

interface CreateAllPairCandidateReportsActionDispatcher : PlayerCandidatesFinder {

    @JsName("actionDispatcher")
    val actionDispatcher: CreatePairCandidateReportActionDispatcher

    private fun CreatePairCandidateReportAction.performThis() = with(actionDispatcher) { perform() }

    fun CreateAllPairCandidateReportsAction.perform() = getReports()
            .ifEmpty { getReportsUsingLongestRule() }

    private fun CreateAllPairCandidateReportsAction.getReportsUsingLongestRule() = game.getReports(PairingRule.LongestTime)

    private fun CreateAllPairCandidateReportsAction.getReports() = game.getReports(game.rule)

    private fun GameSpin.getReports(rule: PairingRule) = remainingPlayers.mapNotNull { player -> pairCandidateReport(rule, player) }

    private fun GameSpin.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport? {
        val candidates = findCandidates(remainingPlayers.toTypedArray(), rule, player)
        return if (candidates.isNotEmpty() || rule == PairingRule.LongestTime) {
            CreatePairCandidateReportAction(player, history, candidates.toList())
                    .performThis()
        } else {
            null
        }
    }
}