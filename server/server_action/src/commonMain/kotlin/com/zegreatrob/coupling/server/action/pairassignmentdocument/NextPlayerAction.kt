package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.CommandExecutor
import com.zegreatrob.coupling.action.SimpleSuccessfulExecutableAction
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue

data class NextPlayerAction(val gameSpin: GameSpin) :
    SimpleSuccessfulExecutableAction<NextPlayerActionDispatcher, PairCandidateReport?> {
    override val perform = link(NextPlayerActionDispatcher::perform)
}

interface NextPlayerActionDispatcher {

    val executor: CommandExecutor<CreatePairCandidateReportsActionDispatcher>

    fun perform(action: NextPlayerAction) = action.lsdkjflsjf()

    private fun NextPlayerAction.lsdkjflsjf() = createPairCandidateReports()
        .fold<PairCandidateReport, PairCandidateReport?>(null) { reportWithLongestTime, report ->
            when {
                reportWithLongestTime == null -> report
                reportWithLongestTime.timeResult == report.timeResult ->
                    withFewestPartners(report, reportWithLongestTime)
                report.timeResult is NeverPaired -> report
                timeSinceLastPairedIsLonger(report, reportWithLongestTime) -> report
                else -> reportWithLongestTime
            }
        }

    private fun NextPlayerAction.createPairCandidateReports() = executor.execute(CreatePairCandidateReportsAction(gameSpin))

    private fun withFewestPartners(report: PairCandidateReport, reportWithLongestTime: PairCandidateReport) =
        when {
            report.partners.size < reportWithLongestTime.partners.size -> report
            else -> reportWithLongestTime
        }

    private fun timeSinceLastPairedIsLonger(
        report: PairCandidateReport,
        reportWithLongestTime: PairCandidateReport
    ) = if (
        report.timeResult is TimeResultValue
        && reportWithLongestTime.timeResult is TimeResultValue
    ) {
        report.timeResult.time > reportWithLongestTime.timeResult.time
    } else {
        false
    }
}
