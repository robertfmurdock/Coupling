package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val id: TribeId, val pinId: String) :
    SimpleSuspendResultAction<DeletePinCommandDispatcher, Unit> {
    override val performFunc = link(DeletePinCommandDispatcher::perform)
}

interface DeletePinCommandDispatcher {
    val pinRepository: PinDelete

    suspend fun perform(command: DeletePinCommand) = with(command) { pinRepository.deletePin(id, pinId) }
        .deletionResult("Pin")

}
