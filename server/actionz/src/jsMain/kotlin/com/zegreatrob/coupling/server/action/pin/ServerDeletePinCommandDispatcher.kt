package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

interface ServerDeletePinCommandDispatcher :
    DeletePinCommand.Dispatcher,
    PinIdDeleteSyntax,
    CurrentPartyIdSyntax {

    override suspend fun perform(command: DeletePinCommand) = command.partyIdPinId()
        .deletePin()
        .voidResult()

    private fun DeletePinCommand.partyIdPinId() = PartyIdPinId(currentPartyId, pinId)
}
