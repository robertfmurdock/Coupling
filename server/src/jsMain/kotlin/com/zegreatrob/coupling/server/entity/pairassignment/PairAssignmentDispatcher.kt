package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.pairassignmentdocument.AssignPinsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportAction
import com.zegreatrob.coupling.action.pairassignmentdocument.CreatePairCandidateReportListAction
import com.zegreatrob.coupling.action.pairassignmentdocument.FindNewPairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.NextPlayerAction
import com.zegreatrob.coupling.action.pairassignmentdocument.ShufflePairsAction
import com.zegreatrob.coupling.action.pairassignmentdocument.Wheel

interface PairAssignmentDispatcher<D> :
    ShufflePairsAction.Dispatcher<D>,
    FindNewPairsAction.Dispatcher<D>,
    NextPlayerAction.Dispatcher<D>,
    CreatePairCandidateReportListAction.Dispatcher<D>,
    CreatePairCandidateReportAction.Dispatcher,
    Wheel where D : CreatePairCandidateReportAction.Dispatcher,
          D : CreatePairCandidateReportListAction.Dispatcher<D>,
          D : NextPlayerAction.Dispatcher<D>,
          D : AssignPinsAction.Dispatcher,
          D : FindNewPairsAction.Dispatcher<D> {
    override val wheel: Wheel get() = this
}
