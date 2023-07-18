package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.Wheel
import com.zegreatrob.testmints.action.ExecutableActionExecutor

interface PairAssignmentDispatcher<D : FindNewPairsAction.Dispatcher> :
    ShufflePairsAction.Dispatcher<D>,
    FindNewPairsAction.Dispatcher,
    NextPlayerAction.Dispatcher,
    CreatePairCandidateReportListAction.Dispatcher,
    CreatePairCandidateReportAction.Dispatcher,
    Wheel {
    override val execute: ExecutableActionExecutor<PairAssignmentDispatcher<D>>
    override val wheel: Wheel get() = this
}
