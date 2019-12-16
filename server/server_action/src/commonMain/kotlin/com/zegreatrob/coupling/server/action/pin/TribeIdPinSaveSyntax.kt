package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.repository.pin.PinSaver
import com.zegreatrob.coupling.model.pin.TribeIdPin

interface TribeIdPinSaveSyntax {

    val pinRepository: PinSaver

    suspend fun TribeIdPin.save() = pinRepository.save(this)

}