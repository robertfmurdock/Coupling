package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SimpleSuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val id: TribeId, val pinId: String) :
    SimpleSuspendAction<DeletePinCommand, DeletePinCommandDispatcher, Unit> {
    override val perform = link(DeletePinCommandDispatcher::perform)
}

interface DeletePinCommandDispatcher {
    val pinRepository: PinDelete

    suspend fun perform(command: DeletePinCommand) = pinRepository.deletePin(command.id, command.pinId)
        .deletionResult("Pin")
}
