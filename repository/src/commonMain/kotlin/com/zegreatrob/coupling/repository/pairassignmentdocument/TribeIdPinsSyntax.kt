package com.zegreatrob.coupling.repository.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinGet

interface TribeIdPinsSyntax {
    val pinRepository: PinGet
    suspend fun TribeId.getPins() = pinRepository.getPins(this)
}