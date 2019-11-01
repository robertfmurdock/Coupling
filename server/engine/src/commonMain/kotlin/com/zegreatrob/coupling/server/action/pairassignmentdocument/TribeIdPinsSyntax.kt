package com.zegreatrob.coupling.server.action.pairassignmentdocument

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.pin.PinGetter

interface TribeIdPinsSyntax {
    val pinRepository: PinGetter
    fun TribeId.getPinsAsync() = pinRepository.getPinsAsync(this)
}