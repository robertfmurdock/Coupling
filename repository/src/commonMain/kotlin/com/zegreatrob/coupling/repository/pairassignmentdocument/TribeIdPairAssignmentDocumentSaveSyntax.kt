package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSaver
    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}