import kotlin.js.JsName

data class GetNextPairAction(val game: Game)

interface GetNextPairActionDispatcher {

    val actionDispatcher: CreateAllPairCandidateReportsActionDispatcher

    private fun CreateAllPairCandidateReportsAction.perform() = with(actionDispatcher) { perform() }

    @JsName("getNextPair")
    fun getNextPair(history: List<HistoryDocument>, players: Array<Player>, rule: PairingRule) = GetNextPairAction(Game(history, players.toList(), rule))
            .perform()


    fun GetNextPairAction.perform() = CreateAllPairCandidateReportsAction(game)
            .perform()
            .fold<PairCandidateReport, PairCandidateReport?>(null) { reportWithLongestTime, report ->
                when {
                    reportWithLongestTime == null -> report
                    reportWithLongestTime.timeResult == report.timeResult -> withFewestPartners(report, reportWithLongestTime)
                    report.timeResult is NeverPaired -> report
                    timeSinceLastPairedIsLonger(report, reportWithLongestTime) -> report
                    else -> reportWithLongestTime
                }
            }

    private fun withFewestPartners(report: PairCandidateReport, reportWithLongestTime: PairCandidateReport): PairCandidateReport {
        return when {
            report.partners.size < reportWithLongestTime.partners.size -> report
            else -> reportWithLongestTime
        }
    }

    private fun timeSinceLastPairedIsLonger(report: PairCandidateReport, reportWithLongestTime: PairCandidateReport): Boolean {
        return if (report.timeResult is TimeResultValue && reportWithLongestTime.timeResult is TimeResultValue) {
            report.timeResult.time > reportWithLongestTime.timeResult.time
        } else {
            false
        }
    }

}