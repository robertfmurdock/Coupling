package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.server.action.SuspendAction
import com.zegreatrob.coupling.server.action.deletionResult

data class DeletePairAssignmentDocumentCommand(val tribeId: TribeId, val id: PairAssignmentDocumentId) :
    SuspendAction<DeletePairAssignmentDocumentCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePairAssignmentDocumentCommandDispatcher) = dispatcher.perform(this)
}

interface DeletePairAssignmentDocumentCommandDispatcher : PairAssignmentDocumentIdDeleteSyntax {

    suspend fun perform(command: DeletePairAssignmentDocumentCommand) = TribeIdPairAssignmentDocumentId(
        command.tribeId, command.id
    )
        .delete()
        .deletionResult("Pair Assignment Document")

}
