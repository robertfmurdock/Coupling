package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentSaver
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) : Action

interface SavePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun SavePairAssignmentDocumentCommand.perform() = logAsync { tribeIdPairAssignmentDocument.apply { save() } }

}

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver

    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
