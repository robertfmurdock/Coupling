package com.zegreatrob.coupling.model.pairassignmentdocument

import com.zegreatrob.coupling.model.pin.PinGetter
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPinsSyntax {
    val pinRepository: PinGetter
    suspend fun TribeId.getPins() = pinRepository.getPins(this)
}