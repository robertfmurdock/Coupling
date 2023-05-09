package com.zegreatrob.coupling.sdk

import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentIdDeleteSyntax

interface ClientDeletePairAssignmentsCommandDispatcher :
    DeletePairAssignmentsCommand.Dispatcher,
    PairAssignmentDocumentIdDeleteSyntax {
    override suspend fun perform(command: DeletePairAssignmentsCommand) = command.compoundId()
        .deleteIt()
        .deletionResult("Pair Assignments")

    fun DeletePairAssignmentsCommand.compoundId() = PartyIdPairAssignmentDocumentId(partyId, pairAssignmentDocumentId)
}
