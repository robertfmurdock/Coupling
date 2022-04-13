package com.zegreatrob.coupling.repository.pin

import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.pin.Pin

interface PartyPinSaveSyntax {

    val pinRepository: PinSave

    suspend fun PartyElement<Pin>.save() =
        pinRepository.save(this)

}