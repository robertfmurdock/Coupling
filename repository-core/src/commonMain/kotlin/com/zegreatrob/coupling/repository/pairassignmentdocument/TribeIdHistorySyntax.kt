package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdHistorySyntax : TribeIdPairAssignmentRecordsSyntax {
    suspend fun TribeId.loadHistory() = loadPairAssignmentRecords().elements
}
