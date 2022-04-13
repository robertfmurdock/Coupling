package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePinCommand(val id: PartyId, val updatedPin: Pin) :
    SimpleSuspendAction<SavePinCommandDispatcher, Unit> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax {

    suspend fun perform(command: SavePinCommand) = command.tribePin()
        .save()

    private fun SavePinCommand.tribePin() = id.with(updatedPin)
}