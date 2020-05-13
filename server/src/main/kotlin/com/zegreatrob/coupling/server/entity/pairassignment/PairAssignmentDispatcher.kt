package com.zegreatrob.coupling.server.entity.pairassignment

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
    Meh,
    Wheel {
    override val executor: CommandExecutor<Meh>
    override val actionDispatcher get() = this
    override val wheel: Wheel get() = this
    override val pairAssignmentDocumentRepository: PairAssignmentDocumentRepository
}

interface Meh : CreatePairCandidateReportsActionDispatcher, CreatePairCandidateReportActionDispatcher