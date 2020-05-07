package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import com.zegreatrob.coupling.action.SuspendAction
import com.zegreatrob.coupling.action.deletionResult

data class DeletePinCommand(val tribeId: TribeId, val pinId: String) :
    SuspendAction<DeletePinCommandDispatcher, Unit> {
    override suspend fun execute(dispatcher: DeletePinCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePinCommandDispatcher : PinIdDeleteSyntax {
    suspend fun DeletePinCommand.perform() = TribeIdPinId(tribeId, pinId).deletePin().deletionResult("Pin")
}

interface PinIdDeleteSyntax {
    val pinRepository: PinDelete
    suspend fun TribeIdPinId.deletePin() = pinRepository.deletePin(tribeId, pinId)
}

