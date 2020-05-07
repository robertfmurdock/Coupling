package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) :
    SuspendAction<SavePairAssignmentDocumentCommandDispatcher, TribeIdPairAssignmentDocument> {
    override suspend fun execute(dispatcher: SavePairAssignmentDocumentCommandDispatcher) = dispatcher.run { perform() }
}

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun SavePairAssignmentDocumentCommand.perform() = tribeIdPairAssignmentDocument.apply { save() }.successResult()
}
