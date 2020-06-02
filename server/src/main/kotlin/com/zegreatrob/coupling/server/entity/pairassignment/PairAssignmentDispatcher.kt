package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*
import com.zegreatrob.testmints.action.ExecutableActionExecutor

interface PairAssignmentDispatcher : ProposeNewPairsCommandDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    RunGameActionDispatcher,
    FindNewPairsActionDispatcher,
    NextPlayerActionDispatcher,
    CreatePairCandidateReportsActionDispatcher,
    CreatePairCandidateReportActionDispatcher,
    Wheel {
    override val execute: ExecutableActionExecutor<PairAssignmentDispatcher>
    override val wheel: Wheel get() = this
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}
