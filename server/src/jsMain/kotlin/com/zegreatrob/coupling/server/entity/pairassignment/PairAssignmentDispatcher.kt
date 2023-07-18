package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.server.action.pairassignmentdocument.Wheel
import com.zegreatrob.testmints.action.ExecutableActionExecutor

interface PairAssignmentDispatcher<D> :
    ShufflePairsAction.Dispatcher<D>,
    FindNewPairsAction.Dispatcher,
    NextPlayerAction.Dispatcher,
    CreatePairCandidateReportListAction.Dispatcher,
    CreatePairCandidateReportAction.Dispatcher,
    Wheel where D : AssignPinsActionDispatcher, D : FindNewPairsAction.Dispatcher {
    override val execute: ExecutableActionExecutor<PairAssignmentDispatcher<D>>
    override val wheel: Wheel get() = this
}
