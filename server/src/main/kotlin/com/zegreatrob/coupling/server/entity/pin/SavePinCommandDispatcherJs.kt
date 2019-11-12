package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise

interface SavePinCommandDispatcherJs : SavePinCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax, JsonSendToResponseSyntax {
    @JsName("performSavePinCommand")
    fun performSavePinCommand(request: Request, response: Response) = scope.promise {
        SavePinCommand(
            TribeIdPin(request.tribeId(), request.body.toPin())
        )
            .perform()
            .toJson()
            .sendTo(response)
    }
}
