package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.pairassignmentdocument.document
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdHistorySyntax : TribeIdPairAssignmentRecordsSyntax {
    suspend fun TribeId.loadHistory() = loadPairAssignmentRecords().data().map { it.document }
}
