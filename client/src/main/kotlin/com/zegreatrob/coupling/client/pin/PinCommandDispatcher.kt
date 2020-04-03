package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.repository.pin.PinRepository

interface PinCommandDispatcher : SavePinCommandDispatcher, DeletePinCommandDispatcher {
    override val pinRepository: PinRepository
}