package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.actionFunc.ResultCommandExecutor
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*

interface PairAssignmentDispatcher : ProposeNewPairsCommandDispatcher,
    SavePairAssignmentDocumentCommandDispatcher,
    DeletePairAssignmentDocumentCommandDispatcher,
    RunGameActionDispatcher,
    FindNewPairsActionDispatcher,
    NextPlayerActionDispatcher,
    CreatePairCandidateReportsActionDispatcher,
    CreatePairCandidateReportActionDispatcher,
    Wheel {
    override val execute: ResultCommandExecutor<PairAssignmentDispatcher>
    override val wheel: Wheel get() = this
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}
