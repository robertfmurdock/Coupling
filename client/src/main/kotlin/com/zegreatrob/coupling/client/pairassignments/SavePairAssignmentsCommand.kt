package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePairAssignmentsCommand(val tribeId: PartyId, val pairAssignments: PairAssignmentDocument) :
    SimpleSuspendAction<SavePairAssignmentsCommandDispatcher, Unit> {
    override val performFunc = link(SavePairAssignmentsCommandDispatcher::perform)
}

interface SavePairAssignmentsCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        tribeId.with(pairAssignments).save()
    }
}
