package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

data class DeletePinCommand(val pinId: String) : SimpleSuspendResultAction<DeletePinCommandDispatcher, Unit> {
    override val performFunc = link(DeletePinCommandDispatcher::perform)
}

interface DeletePinCommandDispatcher : PinIdDeleteSyntax, CurrentPartyIdSyntax {
    suspend fun perform(command: DeletePinCommand) = command.partyIdPinId()
        .deletePin()
        .deletionResult("Pin")

    private fun DeletePinCommand.partyIdPinId() = PartyIdPinId(currentPartyId, pinId)
}
