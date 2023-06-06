package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

interface ServerDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentIdDeleteSyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: DeletePairAssignmentsCommand) = command.partyIdPairAssignmentId()
        .deleteIt()
        .deletionResult("Pair Assignment Document")

    private fun DeletePairAssignmentsCommand.partyIdPairAssignmentId() = PartyIdPairAssignmentDocumentId(
        currentPartyId,
        pairAssignmentDocumentId,
    )
}
