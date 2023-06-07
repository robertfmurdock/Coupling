package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pairassignmentdocument.SavePairAssignmentsCommand
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.PartyIdPairAssignmentDocumentSaveSyntax

interface ClientSavePairAssignmentsCommandDispatcher :
    SavePairAssignmentsCommand.Dispatcher,
    PartyIdPairAssignmentDocumentSaveSyntax {

    override suspend fun perform(command: SavePairAssignmentsCommand) = with(command) { partyId.with(pairAssignments) }
        .save()
        .let { VoidResult.Accepted }
}
