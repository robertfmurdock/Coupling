package com.zegreatrob.coupling.server.entity.pairassignment

import com.zegreatrob.coupling.action.CommandExecutor
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
    override val executor: CommandExecutor<PairAssignmentDispatcher>
    override val wheel: Wheel get() = this
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}
