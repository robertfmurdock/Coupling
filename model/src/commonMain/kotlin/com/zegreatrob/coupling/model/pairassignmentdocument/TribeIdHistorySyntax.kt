package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdHistorySyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter
    suspend fun TribeId.getHistory() = pairAssignmentDocumentRepository.getPairAssignments(this)
}