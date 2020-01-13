package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDeleter

data class DeletePinCommand(val id: TribeId, val pinId: String)

interface DeletePinCommandDispatcher {
    val pinRepository: PinDeleter

    fun DeletePinCommand.perform() {

    }
}