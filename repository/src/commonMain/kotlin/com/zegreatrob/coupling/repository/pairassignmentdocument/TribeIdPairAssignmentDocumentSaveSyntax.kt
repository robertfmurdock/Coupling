package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument

interface TribeIdPairAssignmentDocumentSaveSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentSave
    suspend fun TribeIdPairAssignmentDocument.save() = pairAssignmentDocumentRepository.save(this)
}
