package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val id: TribeId, val pinId: String) : Action

interface DeletePinCommandDispatcher : ActionLoggingSyntax {
    val pinRepository: PinDelete

    suspend fun DeletePinCommand.perform() = logAsync {
        pinRepository.deletePin(id, pinId)
    }
}