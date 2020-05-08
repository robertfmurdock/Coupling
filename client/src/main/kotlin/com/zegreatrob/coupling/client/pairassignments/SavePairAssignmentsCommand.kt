package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax

data class SavePairAssignmentsCommand(val tribeId: TribeId, val pairAssignments: PairAssignmentDocument) :
    SuspendAction<SavePairAssignmentsCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: SavePairAssignmentsCommandDispatcher) = with(dispatcher) { perform() }
}

interface SavePairAssignmentsCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun SavePairAssignmentsCommand.perform() = tribeId.with(pairAssignments).save().successResult()
}
