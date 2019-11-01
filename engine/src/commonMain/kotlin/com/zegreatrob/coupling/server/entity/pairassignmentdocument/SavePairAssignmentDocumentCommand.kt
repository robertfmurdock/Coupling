package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument) : Action

interface SavePairAssignmentDocumentCommandDispatcher : ActionLoggingSyntax, TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun SavePairAssignmentDocumentCommand.perform() = logAsync { tribeIdPairAssignmentDocument.apply { save() } }

}

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver

    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
