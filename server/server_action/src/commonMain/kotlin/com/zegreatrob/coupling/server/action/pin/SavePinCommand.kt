package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.pin.pin
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val tribeIdPin: TribeIdPin) : SimpleSuspendAction<SavePinCommandDispatcher, Pin> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun perform(command: SavePinCommand) = command.skdfjsldkfj().successResult()

    private suspend fun SavePinCommand.skdfjsldkfj() = tribeIdPin.save().let { tribeIdPin.pin }

}