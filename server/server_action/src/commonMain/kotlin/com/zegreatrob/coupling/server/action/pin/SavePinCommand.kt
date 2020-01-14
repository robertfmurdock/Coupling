package com.zegreatrob.coupling.server.action.pin

import com.zegreatrob.coupling.action.Action
import com.zegreatrob.coupling.action.ActionLoggingSyntax
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.repository.pin.TribeIdPinSaveSyntax

data class SavePinCommand(val tribeIdPin: TribeIdPin) : Action

interface SavePinCommandDispatcher : ActionLoggingSyntax,
    TribeIdPinSaveSyntax {

    suspend fun SavePinCommand.perform() = logAsync { tribeIdPin.save().let { tribeIdPin.pin } }

}