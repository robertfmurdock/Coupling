package com.zegreatrob.coupling.entity.pairassignmentdocument

import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.PinRepository

interface TribeIdPinsSyntax {
    val pinRepository: PinRepository

    fun TribeId.getPinsAsync() = pinRepository.getPinsAsync(this)
}