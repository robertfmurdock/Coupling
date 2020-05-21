package com.zegreatrob.coupling.client.pairassignments.list

import com.zegreatrob.coupling.actionFunc.SimpleSuspendResultAction
import com.zegreatrob.coupling.actionFunc.deletionResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

data class DeletePairAssignmentsCommand(val tribeId: TribeId, val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    SimpleSuspendResultAction<DeletePairAssignmentsCommandDispatcher, Unit> {
    override val performFunc = link(DeletePairAssignmentsCommandDispatcher::perform)
}

interface DeletePairAssignmentsCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax {
    suspend fun perform(command: DeletePairAssignmentsCommand) = command.compoundId()
        .delete()
        .deletionResult("Pair Assignments")

    fun DeletePairAssignmentsCommand.compoundId() = TribeIdPairAssignmentDocumentId(tribeId, pairAssignmentDocumentId)
}