package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentSaveSyntax

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) : Action

interface SavePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentSaveSyntax {
    suspend fun SavePairAssignmentDocumentCommand.perform() = logAsync { tribeIdPairAssignmentDocument.apply { save() } }
}

