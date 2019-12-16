package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDeleter

    suspend fun TribeIdPairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(
        tribeId,
        pairAssignmentDocumentId
    )
}