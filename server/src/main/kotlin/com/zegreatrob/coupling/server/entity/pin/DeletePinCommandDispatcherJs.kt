package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise

interface DeletePinCommandDispatcherJs : DeletePinCommandDispatcher, ScopeSyntax {
    @JsName("performDeletePinCommand")
    fun performDeletePinCommand(pinId: String) = scope.promise {
        DeletePinCommand(TribeId(""), pinId)
            .perform()
    }
}
