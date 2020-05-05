package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher

interface PinDispatcher : SavePinCommandDispatcher, DeletePinCommandDispatcher {
    override val pinRepository: PinRepository
}
