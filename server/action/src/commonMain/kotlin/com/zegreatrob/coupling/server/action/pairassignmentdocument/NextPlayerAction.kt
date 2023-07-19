package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.testmints.action.ExecutableActionExecutor
import com.zegreatrob.testmints.action.async.SimpleSuspendAction
import kotools.types.collection.NotEmptyList

data class NextPlayerAction(val gameSpin: GameSpin) :
    SimpleSuspendAction<NextPlayerAction.Dispatcher<*>, PairCandidateReport> {
    override val performFunc = link(Dispatcher<*>::perform)

    interface Dispatcher<out D> {

        val execute: ExecutableActionExecutor<CreatePairCandidateReportListAction.Dispatcher>

        suspend fun perform(action: NextPlayerAction): PairCandidateReport = with(action.createPairCandidateReports()) {
            toList().fold(head) { reportWithLongestTime, report ->
                when {
                    reportWithLongestTime.timeResult == report.timeResult ->
                        withFewestPartners(report, reportWithLongestTime)
                    report.timeResult is NeverPaired -> report
                    timeSinceLastPairedIsLonger(report, reportWithLongestTime) -> report
                    else -> reportWithLongestTime
                }
            }
        }

        private fun NextPlayerAction.createPairCandidateReports(): NotEmptyList<PairCandidateReport> =
            execute(CreatePairCandidateReportListAction(gameSpin))

        private fun withFewestPartners(report: PairCandidateReport, reportWithLongestTime: PairCandidateReport) =
            when {
                report.partners.size < reportWithLongestTime.partners.size -> report
                else -> reportWithLongestTime
            }

        private fun timeSinceLastPairedIsLonger(
            report: PairCandidateReport,
            reportWithLongestTime: PairCandidateReport,
        ) = if (
            report.timeResult is TimeResultValue &&
            reportWithLongestTime.timeResult is TimeResultValue
        ) {
            report.timeResult.time > reportWithLongestTime.timeResult.time
        } else {
            false
        }
    }
}
