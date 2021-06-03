package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class SavePairAssignmentDocumentCommand(val pairAssignmentDocument: PairAssignmentDocument) :
    SimpleSuspendResultAction<SavePairAssignmentDocumentCommandDispatcher, TribeIdPairAssignmentDocument> {
    override val performFunc = link(SavePairAssignmentDocumentCommandDispatcher::perform)
}

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax, CurrentTribeIdSyntax {
    suspend fun perform(command: SavePairAssignmentDocumentCommand) =
        currentTribeId.with(command.pairAssignmentDocument)
            .apply { save() }
            .successResult()
}
