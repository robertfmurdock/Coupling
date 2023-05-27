package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.server.action.connection.CurrentPartyIdSyntax

data class DeletePinCommand(val pinId: String) : SimpleSuspendResultAction<DeletePinCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher : PinIdDeleteSyntax, CurrentPartyIdSyntax {
        suspend fun perform(command: DeletePinCommand) = command.partyIdPinId()
            .deletePin()
            .deletionResult("Pin")

        private fun DeletePinCommand.partyIdPinId() = PartyIdPinId(currentPartyId, pinId)
    }
}
