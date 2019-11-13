package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.PerformJsonHandlingSyntax
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.sendSuccessful

interface SavePinCommandDispatcherJs : SavePinCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    JsonSendToResponseSyntax,
    PerformJsonHandlingSyntax {
    @JsName("performSavePinCommand")
    fun performSavePinCommand(request: Request, response: Response) =
        performJsonHandling(request, response, Response::sendSuccessful, ::handleSavePinCommand)

    private suspend fun handleSavePinCommand(request: Request) = request.savePinCommand()
        .perform()
        .toJson()

    private fun Request.savePinCommand() = SavePinCommand(
        TribeIdPin(tribeId(), jsonBody().toPin())
    )
}
