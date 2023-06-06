package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.pin.SavePinCommand
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.PartyPinSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

interface ServerSavePinCommandDispatcher :
    SavePinCommand.Dispatcher,
    PartyPinSaveSyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: SavePinCommand) = command.save().let { Unit.successResult() }

    private suspend fun SavePinCommand.save() = currentPartyId.with(pin).save().let { pin }
}
