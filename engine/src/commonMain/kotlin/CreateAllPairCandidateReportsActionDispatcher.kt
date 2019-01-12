import kotlin.js.JsName

data class CreateAllPairCandidateReportsAction(val game: Game)

interface CreateAllPairCandidateReportsActionDispatcher : PlayerCandidatesFinder {

    @JsName("actionDispatcher")
    val actionDispatcher: CreatePairCandidateReportActionDispatcher

    private fun CreatePairCandidateReportAction.performThis() = with(actionDispatcher) { perform() }

    @JsName("createAllPairCandidateReport")
    fun createAllPairCandidateReport(history: List<HistoryDocument>, players: Array<Player>, rule: PairingRule) =
            CreateAllPairCandidateReportsAction(Game(history, players.toList(), rule))
                    .perform()
                    .toTypedArray()

    fun CreateAllPairCandidateReportsAction.perform() = getReports()
            .ifEmpty { getReportsUsingLongestRule() }

    private fun CreateAllPairCandidateReportsAction.getReportsUsingLongestRule() = game.getReports(PairingRule.LongestTime)

    private fun CreateAllPairCandidateReportsAction.getReports() = game.getReports(game.rule)

    private fun Game.getReports(rule: PairingRule) = players.mapNotNull { player -> pairCandidateReport(rule, player) }

    private fun Game.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport? {
        val candidates = findCandidates(players.toTypedArray(), rule, player)
        return if (candidates.isNotEmpty() || rule == PairingRule.LongestTime) {
            CreatePairCandidateReportAction(history, player, candidates.toList())
                    .performThis()
        } else {
            null
        }
    }
}