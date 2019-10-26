package com.zegreatrob.coupling.server.entity.pairassignmentdocument

import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.server.entity.pin.PinGetter

interface TribeIdPinsSyntax {
    val pinRepository: PinGetter
    fun TribeId.getPinsAsync() = pinRepository.getPinsAsync(this)
}