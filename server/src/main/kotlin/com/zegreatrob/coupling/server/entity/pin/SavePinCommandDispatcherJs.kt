package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import kotlinx.coroutines.promise
import kotlin.js.Json

interface SavePinCommandDispatcherJs : SavePinCommandDispatcher, ScopeSyntax {
    @JsName("performSavePinCommand")
    fun performSavePinCommand(pin: Json, tribeId: String) = scope.promise {
        SavePinCommand(
            TribeIdPin(TribeId(tribeId), pin.toPin())
        )
            .perform()
            .toJson()
    }
}
