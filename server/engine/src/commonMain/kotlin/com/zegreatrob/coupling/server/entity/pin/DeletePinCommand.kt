package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pin.PinDeleter

data class DeletePinCommand(val pinId: String) : Action

interface DeletePinCommandDispatcher : ActionLoggingSyntax, PinIdDeleteSyntax {
    suspend fun DeletePinCommand.perform() = logAsync { pinId.run { deletePin() } }
}

interface PinIdDeleteSyntax {
    val pinRepository: PinDeleter
    suspend fun String.deletePin() = pinRepository.deletePin(this)
}
