package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.repository.pin.PinRepository
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher

interface PinDispatcher : SavePinCommandDispatcher {
    override val pinRepository: PinRepository
}
