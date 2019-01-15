data class GetNextPairAction(val gameSpin: GameSpin)

interface GetNextPairActionDispatcher {

    val actionDispatcher: CreateAllPairCandidateReportsActionDispatcher

    private fun CreateAllPairCandidateReportsAction.performThis() = with(actionDispatcher) { perform() }

    fun GetNextPairAction.perform() = CreateAllPairCandidateReportsAction(gameSpin)
            .performThis()
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