package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pin.PinGet

interface TribeIdPinRecordsSyntax {
    val pinRepository: PinGet
    suspend fun PartyId.getPinRecords() = pinRepository.getPins(this)
}