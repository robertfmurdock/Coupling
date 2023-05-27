package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

data class DeletePairAssignmentDocumentCommand(val pairAssignmentDocumentId: PairAssignmentDocumentId) :
    SimpleSuspendResultAction<DeletePairAssignmentDocumentCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PairAssignmentDocumentIdDeleteSyntax, CurrentPartyIdSyntax {

        suspend fun perform(command: DeletePairAssignmentDocumentCommand) = command.partyIdPairAssignmentId()
            .deleteIt()
            .deletionResult("Pair Assignment Document")

        private fun DeletePairAssignmentDocumentCommand.partyIdPairAssignmentId() = PartyIdPairAssignmentDocumentId(
            currentPartyId,
            pairAssignmentDocumentId,
        )
    }
}
