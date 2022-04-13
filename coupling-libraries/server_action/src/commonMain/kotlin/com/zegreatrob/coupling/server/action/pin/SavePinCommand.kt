package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.SimpleSuspendResultAction
import com.zegreatrob.coupling.action.successResult
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.party.with
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax
import com.zegreatrob.coupling.server.action.connection.CurrentTribeIdSyntax

data class SavePinCommand(val pin: Pin) : SimpleSuspendResultAction<SavePinCommandDispatcher, Pin> {
    override val performFunc = link(SavePinCommandDispatcher::perform)
}

interface SavePinCommandDispatcher : TribeIdPinSaveSyntax, CurrentTribeIdSyntax {

    suspend fun perform(command: SavePinCommand) = command.save().successResult()

    private suspend fun SavePinCommand.save() = currentPartyId.with(pin).save().let { pin }

}