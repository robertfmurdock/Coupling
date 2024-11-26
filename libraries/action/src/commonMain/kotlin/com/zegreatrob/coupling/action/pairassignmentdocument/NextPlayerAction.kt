package com.zegreatrob.coupling.action.pairassignmentdocument

import com.zegreatrob.coupling.action.CannonProvider
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotools.types.collection.NotEmptyList

@ActionMint
data class NextPlayerAction(val gameSpin: GameSpin) {

    interface Dispatcher<out D> : CannonProvider<D>
        where D : CreatePairCandidateReportAction.Dispatcher,
              D : CreatePairCandidateReportListAction.Dispatcher<D> {

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

        private suspend fun NextPlayerAction.createPairCandidateReports(): NotEmptyList<PairCandidateReport> = cannon.fire(CreatePairCandidateReportListAction(gameSpin))

        private fun withFewestPartners(report: PairCandidateReport, reportWithLongestTime: PairCandidateReport) = when {
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
