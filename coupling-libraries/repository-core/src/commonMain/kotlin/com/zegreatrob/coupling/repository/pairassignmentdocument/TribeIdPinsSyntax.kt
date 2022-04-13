package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface TribeIdPinsSyntax : TribeIdPinRecordsSyntax {
    suspend fun PartyId.getPins() = getPinRecords().elements
}

