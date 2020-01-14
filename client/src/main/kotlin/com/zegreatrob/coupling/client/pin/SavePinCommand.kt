package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val id: TribeId, val updatedPin: Pin)

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun SavePinCommand.perform() {
        TribeIdPin(id, updatedPin)
            .save()
    }
}