package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.CreatePairCandidateReportsActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.FindNewPairsActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.NextPlayerActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.RunGameActionDispatcher
import com.zegreatrob.coupling.server.action.pairassignmentdocument.Wheel
import com.zegreatrob.testmints.action.ExecutableActionExecutor

interface PairAssignmentDispatcher :
    RunGameActionDispatcher,
    FindNewPairsActionDispatcher,
    NextPlayerActionDispatcher,
    CreatePairCandidateReportsActionDispatcher,
    CreatePairCandidateReportActionDispatcher,
    Wheel {
    override val execute: ExecutableActionExecutor<PairAssignmentDispatcher>
    override val wheel: Wheel get() = this
}
