package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDelete

    suspend fun TribeIdPairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(
        partyId,
        pairAssignmentDocumentId
    )
}