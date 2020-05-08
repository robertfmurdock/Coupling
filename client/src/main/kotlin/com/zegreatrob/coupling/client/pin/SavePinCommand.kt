package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val id: TribeId, val updatedPin: Pin) : SuspendAction<SavePinCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: SavePinCommandDispatcher) = with(dispatcher) { perform() }
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun SavePinCommand.perform() = id.with(updatedPin)
        .save()
        .successResult()
}