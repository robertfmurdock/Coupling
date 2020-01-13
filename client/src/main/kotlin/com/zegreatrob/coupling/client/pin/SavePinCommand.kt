package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinSaver

data class SavePinCommand(val id: TribeId, val updatedPin: Pin)

interface SavePinCommandDispatcher {
    val pinRepository: PinSaver

    fun SavePinCommand.perform() {

    }
}