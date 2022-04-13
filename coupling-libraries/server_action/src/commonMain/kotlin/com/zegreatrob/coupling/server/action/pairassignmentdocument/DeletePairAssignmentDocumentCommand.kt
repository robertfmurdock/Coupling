package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class DeletePairAssignmentDocumentCommand(val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    SimpleSuspendResultAction<DeletePairAssignmentDocumentCommandDispatcher, Unit> {
    override val performFunc = link(DeletePairAssignmentDocumentCommandDispatcher::perform)
}

interface DeletePairAssignmentDocumentCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax, CurrentTribeIdSyntax {

    suspend fun perform(command: DeletePairAssignmentDocumentCommand) = command.tribeIdPairAssignmentId()
        .delete()
        .deletionResult("Pair Assignment Document")

    private fun DeletePairAssignmentDocumentCommand.tribeIdPairAssignmentId() = PartyIdPairAssignmentDocumentId(
        currentPartyId, pairAssignmentDocumentId
    )

}
