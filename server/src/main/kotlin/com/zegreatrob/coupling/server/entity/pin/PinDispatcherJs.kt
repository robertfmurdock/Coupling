package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.repository.pin.PinRepository

interface PinDispatcherJs : SavePinCommandDispatcherJs, PinsQueryDispatcherJs, DeletePinCommandDispatcherJs {
    override val pinRepository: PinRepository
}
