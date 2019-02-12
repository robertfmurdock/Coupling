package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.pairassignmentdocument.TribeIdPairAssignmentDocument

data class SavePairAssignmentDocumentCommand(val tribeIdPairAssignmentDocument: TribeIdPairAssignmentDocument)

interface SavePairAssignmentDocumentCommandDispatcher : TribeIdPairAssignmentDocumentSaveSyntax {

    suspend fun SavePairAssignmentDocumentCommand.perform() = tribeIdPairAssignmentDocument.apply { save() }

}

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver

    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
