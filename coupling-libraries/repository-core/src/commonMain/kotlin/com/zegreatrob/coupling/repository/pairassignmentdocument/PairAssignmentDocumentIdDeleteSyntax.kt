package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.pairassignmentdocument.PartyIdPairAssignmentDocumentId

interface PairAssignmentDocumentIdDeleteSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentDelete

    suspend fun PartyIdPairAssignmentDocumentId.delete() = pairAssignmentDocumentRepository.delete(
        partyId,
        pairAssignmentDocumentId
    )
}
