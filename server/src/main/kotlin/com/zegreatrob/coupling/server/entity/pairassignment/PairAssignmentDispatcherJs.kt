package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*

interface PairAssignmentDispatcherJs : ProposeNewPairsCommandDispatcherJs,
    SavePairAssignmentDocumentCommandDispatcherJs,
    DeletePairAssignmentDocumentCommandDispatcherJs, PairAssignmentDocumentListQueryDispatcherJs,
    RunGameActionDispatcher,
    FindNewPairsActionDispatcher,
    NextPlayerActionDispatcher,
    CreatePairCandidateReportsActionDispatcher,
    CreatePairCandidateReportActionDispatcher,
    Wheel {
    override val actionDispatcher get() = this
    override val wheel: Wheel get() = this
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}
