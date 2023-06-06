package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax

interface ClientSavePinCommandDispatcher : PartyPinSaveSyntax, SavePinCommand.Dispatcher {

    override suspend fun perform(command: SavePinCommand) = command.partyPin()
        .save()
        .let { Unit.successResult() }

    private fun SavePinCommand.partyPin() = id.with(pin)
}
