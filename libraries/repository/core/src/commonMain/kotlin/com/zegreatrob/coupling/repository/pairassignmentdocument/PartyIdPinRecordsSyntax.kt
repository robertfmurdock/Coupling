package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pin.PinGet

interface PartyIdPinRecordsSyntax {
    val pinRepository: PinGet
    suspend fun PartyId.loadPins() = pinRepository.getPins(this)
}
