package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.core.entity.tribe.TribeId

interface TribeIdHistorySyntax {
    val pairAssignmentDocumentRepository: PairAssignmentDocumentGetter
    fun TribeId.getHistoryAsync() = pairAssignmentDocumentRepository.getPairAssignmentsAsync(this)
}