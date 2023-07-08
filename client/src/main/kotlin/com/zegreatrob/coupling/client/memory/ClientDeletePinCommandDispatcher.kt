package com.zegreatrob.coupling.client.memory

import com.zegreatrob.coupling.action.pin.DeletePinCommand
import com.zegreatrob.coupling.action.voidResult
import com.zegreatrob.coupling.repository.pin.PinDelete

interface ClientDeletePinCommandDispatcher : DeletePinCommand.Dispatcher {
    val pinRepository: PinDelete

    override suspend fun perform(command: DeletePinCommand) = with(command) { pinRepository.deletePin(partyId, pinId) }
        .voidResult()
}
