data class CreatePairCandidateReportsAction(val game: GameSpin)

interface CreatePairCandidateReportsActionDispatcher : PlayerCandidatesFinder {

    @JsName("actionDispatcher")
    val actionDispatcher: CreatePairCandidateReportActionDispatcher

    private fun CreatePairCandidateReportAction.performThis() = with(actionDispatcher) { perform() }

    fun CreatePairCandidateReportsAction.perform() = createReports()
            .ifEmpty { createReportsUsingLongestRule() }

    private fun CreatePairCandidateReportsAction.createReportsUsingLongestRule() = game.createReports(PairingRule.LongestTime)

    private fun CreatePairCandidateReportsAction.createReports() = game.createReports(game.rule)

    private fun GameSpin.createReports(rule: PairingRule) = remainingPlayers.mapNotNull { player -> pairCandidateReport(rule, player) }

    private fun GameSpin.pairCandidateReport(rule: PairingRule, player: Player): PairCandidateReport? {
        val candidates = findCandidates(remainingPlayers, rule, player)
        return if (candidates.isNotEmpty() || rule == PairingRule.LongestTime) {
            createReport(player, candidates)
        } else {
            null
        }
    }

    private fun GameSpin.createReport(player: Player, candidates: Array<Player>) =
            CreatePairCandidateReportAction(player, history, candidates.toList())
                    .performThis()
}