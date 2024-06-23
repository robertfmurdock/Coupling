package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.VoidResult
import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax

interface ClientSavePinCommandDispatcher :
    PartyPinSaveSyntax,
    SavePinCommand.Dispatcher {

    override suspend fun perform(command: SavePinCommand) = command.partyPin()
        .save()
        .let { VoidResult.Accepted }

    private fun SavePinCommand.partyPin() = id.with(pin)
}
