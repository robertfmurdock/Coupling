package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGet

interface TribeIdPinRecordsSyntax {
    val pinRepository: PinGet
    suspend fun TribeId.getPinRecords() = pinRepository.getPins(this)
}