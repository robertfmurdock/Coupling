package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.JsonSendToResponseSyntax
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.entity.tribe.RequestTribeIdSyntax
import com.zegreatrob.coupling.server.entity.tribe.ScopeSyntax
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import kotlinx.coroutines.promise
import kotlin.js.json

interface DeletePinCommandDispatcherJs : DeletePinCommandDispatcher, ScopeSyntax, RequestTribeIdSyntax,
    RequestPinIdSyntax, JsonSendToResponseSyntax {
    @JsName("performDeletePinCommand")
    fun performDeletePinCommand(request: Request, response: Response) = scope.promise {
        val success = DeletePinCommand(request.tribeId(), request.pinId())
            .perform()
        sendResult(success, response)
    }

    private fun sendResult(success: Boolean, response: Response) {
        if (success) {
            response.sendStatus(200)
        } else {
            json("message" to "Failed to remove the pin because it did not exist.")
                .sendTo(response, 404)
        }
    }
}
