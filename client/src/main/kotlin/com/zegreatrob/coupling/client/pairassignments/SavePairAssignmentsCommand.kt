package com.zegreatrob.coupling.client.pairassignments

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePairAssignmentsCommand(val tribeId: TribeId, val pairAssignments: PairAssignmentDocument) :
    SimpleSuspendAction<SavePairAssignmentsCommandDispatcher, Unit> {
    override val performFunc = link(SavePairAssignmentsCommandDispatcher::perform)
}

interface SavePairAssignmentsCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun perform(command: SavePairAssignmentsCommand) = with(command) {
        tribeId.with(pairAssignments).save()
    }
}
