package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.deletionResult
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val id: TribeId, val pinId: String) : SuspendAction<DeletePinCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePinCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePinCommandDispatcher {
    val pinRepository: PinDelete

    suspend fun DeletePinCommand.perform() = pinRepository.deletePin(id, pinId).deletionResult("Pin")
}