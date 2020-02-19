package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPairAssignmentRecordsSyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    suspend fun TribeId.loadPairAssignmentRecords() = pairAssignmentDocumentRepository.getPairAssignmentRecords(this)
}
