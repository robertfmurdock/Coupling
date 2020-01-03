package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdHistorySyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGet
    suspend fun TribeId.loadHistory() = pairAssignmentDocumentRepository.getPairAssignments(this)
}