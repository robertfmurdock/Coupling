package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val tribeId: TribeId, val pinId: String) : Action

interface DeletePinCommandDispatcher : ActionLoggingSyntax, PinIdDeleteSyntax {
    suspend fun DeletePinCommand.perform() = logAsync { TribeIdPinId(tribeId, pinId).deletePin() }
}

interface PinIdDeleteSyntax {
    val pinRepository: PinDelete
    suspend fun TribeIdPinId.deletePin() = pinRepository.deletePin(tribeId, pinId)
}

