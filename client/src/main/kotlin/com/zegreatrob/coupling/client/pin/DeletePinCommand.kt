package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.repository.pin.PinDelete

data class DeletePinCommand(val id: TribeId, val pinId: String)

interface DeletePinCommandDispatcher {
    val pinRepository: PinDelete

    fun DeletePinCommand.perform() {

    }
}