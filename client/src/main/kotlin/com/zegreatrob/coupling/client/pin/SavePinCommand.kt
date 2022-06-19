package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax
import com.zegreatrob.testmints.action.async.SimpleSuspendAction

data class SavePinCommand(val id: PartyId, val updatedPin: Pin) : SimpleSuspendAction<SavePinCommandDispatcher, Unit> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : PartyPinSaveSyntax {

    suspend fun perform(command: SavePinCommand) = command.partyPin()
        .save()

    private fun SavePinCommand.partyPin() = id.with(updatedPin)
}
