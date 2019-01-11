import kotlin.js.JsName

data class GetNextPairAction(val history: List<HistoryDocument>, val players: List<Player>, val rule: PairingRule)

interface GetNextPairActionDispatcher {

    val actionDispatcher: CreateAllPairCandidateReportsCommandDispatcher

    private fun CreateAllPairCandidateReportsCommand.perform() = with(actionDispatcher) { perform() }

    @JsName("getNextPair")
    fun getNextPair(history: List<HistoryDocument>, players: Array<Player>, rule: PairingRule) = GetNextPairAction(history, players.toList(), rule)
            .perform()


    fun GetNextPairAction.perform() = CreateAllPairCandidateReportsCommand(history, players, rule)
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