package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.ActionLoggingSyntax

data class DeletePinCommand(val pinId: String) : Action

interface DeletePinCommandDispatcher : ActionLoggingSyntax, PinIdDeleteSyntax {
    suspend fun DeletePinCommand.perform() = logAsync { pinId.run { deletePin() } }
}

interface PinIdDeleteSyntax {
    val pinRepository: PinDeleter
    suspend fun String.deletePin() = pinRepository.deletePin(this)
}
