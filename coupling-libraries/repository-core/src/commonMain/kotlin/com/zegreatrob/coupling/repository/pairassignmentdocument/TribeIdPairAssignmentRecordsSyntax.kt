package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.PartyId

interface TribeIdPairAssignmentRecordsSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    suspend fun PartyId.loadPairAssignmentRecords() = pairAssignmentDocumentRepository.getPairAssignments(this)
}
