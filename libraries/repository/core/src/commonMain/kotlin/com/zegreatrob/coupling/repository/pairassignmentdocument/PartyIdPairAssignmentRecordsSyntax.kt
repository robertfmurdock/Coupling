package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdPairAssignmentRecordsSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    suspend fun PartyId.loadPairAssignmentRecords() = pairAssignmentDocumentRepository.loadPairAssignments(this)
}
