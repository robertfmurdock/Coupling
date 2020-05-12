package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId

data class DeletePinCommand(val tribeId: TribeId, val pinId: String) :
    SimpleSuspendAction<DeletePinCommandDispatcher, Unit> {
    override val perform = link(DeletePinCommandDispatcher::perform)
}

interface DeletePinCommandDispatcher : PinIdDeleteSyntax {
    suspend fun perform(command: DeletePinCommand) = command.tribeIdPinId()
        .deletePin()
        .deletionResult("Pin")

    private fun DeletePinCommand.tribeIdPinId() = TribeIdPinId(tribeId, pinId)
}
