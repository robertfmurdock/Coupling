package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.actionFunc.SimpleSuspendAction
import com.zegreatrob.coupling.actionFunc.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val id: TribeId, val updatedPin: Pin) :
    SimpleSuspendAction<SavePinCommandDispatcher, Unit> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun perform(command: SavePinCommand) = command.tribePin()
        .save()
        .successResult()

    private fun SavePinCommand.tribePin() = id.with(updatedPin)
}