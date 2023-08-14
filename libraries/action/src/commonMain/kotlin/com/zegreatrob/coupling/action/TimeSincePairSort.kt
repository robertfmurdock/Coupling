package com.zegreatrob.coupling.action

import com.zegreatrob.coupling.action.stats.PairReport
import com.zegreatrob.coupling.model.pairassignmentdocument.NeverPaired
import com.zegreatrob.coupling.model.pairassignmentdocument.TimeResultValue

fun timeSincePairSort(report: PairReport) = when (val time = report.timeSinceLastPair) {
    NeverPaired -> Int.MAX_VALUE
    is TimeResultValue -> time.time
}
