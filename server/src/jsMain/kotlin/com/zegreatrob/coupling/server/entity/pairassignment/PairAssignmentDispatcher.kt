package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.Wheel
import com.zegreatrob.testmints.action.ExecutableActionExecutor

interface PairAssignmentDispatcher :
    ShufflePairsAction.Dispatcher,
    FindNewPairsAction.Dispatcher,
    NextPlayerAction.Dispatcher,
    CreatePairCandidateReportListAction.Dispatcher,
    CreatePairCandidateReportAction.Dispatcher,
    Wheel {
    override val execute: ExecutableActionExecutor<PairAssignmentDispatcher>
    override val wheel: Wheel get() = this
}
