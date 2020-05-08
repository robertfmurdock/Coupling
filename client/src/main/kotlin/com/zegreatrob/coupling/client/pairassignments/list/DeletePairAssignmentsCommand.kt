package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

data class DeletePairAssignmentsCommand(val tribeId: TribeId, val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    SuspendAction<DeletePairAssignmentsCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePairAssignmentsCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePairAssignmentsCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax {
    suspend fun DeletePairAssignmentsCommand.perform() = compoundId()
        .delete()
        .deletionResult("Pair Assignments")

    fun DeletePairAssignmentsCommand.compoundId() =
        TribeIdPairAssignmentDocumentId(tribeId, pairAssignmentDocumentId)
}