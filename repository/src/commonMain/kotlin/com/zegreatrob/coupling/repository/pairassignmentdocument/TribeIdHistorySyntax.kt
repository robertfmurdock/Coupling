package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdHistorySyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter
    suspend fun TribeId.loadHistory() = pairAssignmentDocumentRepository.getPairAssignments(this)
}