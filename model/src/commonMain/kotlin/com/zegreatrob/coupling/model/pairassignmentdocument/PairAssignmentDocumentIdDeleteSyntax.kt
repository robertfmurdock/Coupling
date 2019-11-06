package com.zegreatrob.coupling.model.pairassignmentdocument

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDeleter

    suspend fun TribeIdPairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(
        tribeId,
        pairAssignmentDocumentId
    )
}