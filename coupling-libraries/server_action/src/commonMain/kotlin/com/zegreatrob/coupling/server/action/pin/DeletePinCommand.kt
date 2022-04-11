package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class DeletePinCommand(val pinId: String) : SimpleSuspendResultAction<DeletePinCommandDispatcher, Unit> {
    override val performFunc = link(DeletePinCommandDispatcher::perform)
}

interface DeletePinCommandDispatcher : PinIdDeleteSyntax, CurrentTribeIdSyntax {
    suspend fun perform(command: DeletePinCommand) = command.tribeIdPinId()
        .deletePin()
        .deletionResult("Pin")

    private fun DeletePinCommand.tribeIdPinId() = TribeIdPinId(currentPartyId, pinId)
}
