package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.pin.TribeIdPin

interface TribeIdPinSaveSyntax {

    val pinRepository: PinSave

    suspend fun TribeIdPin.save() = pinRepository.save(this)

}