package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGetter

interface TribeIdPinsSyntax {
    val pinRepository: PinGetter
    suspend fun TribeId.getPins() = pinRepository.getPins(this)
}