package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete
import com.zegreatrob.coupling.server.action.SuspendAction

data class DeletePinCommand(val tribeId: TribeId, val pinId: String) :
    SuspendAction<DeletePinCommandDispatcher, Boolean> {
    override suspend fun execute(dispatcher: DeletePinCommandDispatcher) = with(dispatcher) { perform() }
}

interface DeletePinCommandDispatcher : PinIdDeleteSyntax {
    suspend fun DeletePinCommand.perform() = TribeIdPinId(tribeId, pinId).deletePin()
}

interface PinIdDeleteSyntax {
    val pinRepository: PinDelete
    suspend fun TribeIdPinId.deletePin() = pinRepository.deletePin(tribeId, pinId)
}

