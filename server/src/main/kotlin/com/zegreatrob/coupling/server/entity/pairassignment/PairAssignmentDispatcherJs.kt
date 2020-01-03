package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.server.action.pairassignmentdocument.*

interface PairAssignmentDispatcherJs : ProposeNewPairsCommandDispatcherJs,
    SavePairAssignmentDocumentCommandDispatcherJs,
    DeletePairAssignmentDocumentCommandDispatcherJs,
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
