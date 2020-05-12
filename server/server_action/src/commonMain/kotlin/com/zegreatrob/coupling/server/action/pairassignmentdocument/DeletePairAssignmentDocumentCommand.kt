package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

data class DeletePairAssignmentDocumentCommand(val tribeId: TribeId, val id: PairAssignmentDocumentId) :
    SimpleSuspendAction<DeletePairAssignmentDocumentCommandDispatcher, Unit> {
    override val perform = link(DeletePairAssignmentDocumentCommandDispatcher::perform)
}

interface DeletePairAssignmentDocumentCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax {

    suspend fun perform(command: DeletePairAssignmentDocumentCommand) = command.tribeIdPairAssignmentId()
        .delete()
        .deletionResult("Pair Assignment Document")

    private fun DeletePairAssignmentDocumentCommand.tribeIdPairAssignmentId() = TribeIdPairAssignmentDocumentId(
        tribeId, id
    )

}
