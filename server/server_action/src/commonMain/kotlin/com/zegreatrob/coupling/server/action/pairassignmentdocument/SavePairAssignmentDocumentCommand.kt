package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) :
    SimpleSuspendAction<SavePairAssignmentDocumentCommandDispatcher, TribeIdPairAssignmentDocument> {
    override val performFunc = link(SavePairAssignmentDocumentCommandDispatcher::perform)
}

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun perform(command: SavePairAssignmentDocumentCommand) = command.tribeIdPairAssignmentDocument
        .apply { save() }
        .successResult()
}
