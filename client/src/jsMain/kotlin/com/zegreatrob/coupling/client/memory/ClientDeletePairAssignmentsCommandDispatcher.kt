package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

interface ClientDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentIdDeleteSyntax {

    override suspend fun perform(command: DeletePairAssignmentsCommand) = command.compoundId()
        .deleteIt()
        .voidResult()

    private fun DeletePairAssignmentsCommand.compoundId() = PartyIdPairAssignmentDocumentId(partyId, pairAssignmentDocumentId)
}
