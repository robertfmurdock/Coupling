package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdHistorySyntax : TribeIdPairAssignmentRecordsSyntax {
    suspend fun PartyId.loadHistory() = loadPairAssignmentRecords().elements
}
