package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) :
    SimpleSuspendResultAction<SavePairAssignmentDocumentCommandDispatcher, TribeIdPairAssignmentDocument> {
    override val performFunc = link(SavePairAssignmentDocumentCommandDispatcher::perform)
}

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun perform(command: SavePairAssignmentDocumentCommand) = command.tribeIdPairAssignmentDocument
        .apply { save() }
        .successResult()
}
