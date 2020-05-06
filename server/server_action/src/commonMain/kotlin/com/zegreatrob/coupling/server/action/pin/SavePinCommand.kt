package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax
import com.zegreatrob.coupling.server.action.SuspendAction

data class SavePinCommand(val tribeIdPin: TribeIdPin) : SuspendAction<SavePinCommandDispatcher, Pin> {
    override suspend fun execute(dispatcher: SavePinCommandDispatcher) = with(dispatcher) { perform() }
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun SavePinCommand.perform() = tribeIdPin.save().let { tribeIdPin.pin }

}