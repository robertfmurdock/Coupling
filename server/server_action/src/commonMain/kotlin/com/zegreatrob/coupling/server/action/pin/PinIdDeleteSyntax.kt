package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.repository.pin.PinDelete

interface PinIdDeleteSyntax {
    val pinRepository: PinDelete
    suspend fun TribeIdPinId.deletePin() = pinRepository.deletePin(tribeId, pinId)
}