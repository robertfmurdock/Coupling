package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val id: TribeId, val updatedPin: Pin) :
    SimpleSuspendResultAction<SavePinCommandDispatcher, Unit> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun perform(command: SavePinCommand) = command.tribePin()
        .save()
        .successResult()

    private fun SavePinCommand.tribePin() = id.with(updatedPin)
}