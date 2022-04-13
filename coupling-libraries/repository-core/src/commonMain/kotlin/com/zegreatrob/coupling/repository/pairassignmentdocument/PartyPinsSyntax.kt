package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId

interface PartyPinsSyntax : PartyIdPinRecordsSyntax {
    suspend fun PartyId.getPins() = getPinRecords().elements
}

