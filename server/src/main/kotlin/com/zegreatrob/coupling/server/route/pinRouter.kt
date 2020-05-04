package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*

val pinRouter = Router(routerParams(mergeParams = true)).apply {
    route("/")
        .post(handleRequest { endpointHandler(Response::sendSuccessful, ::handleSavePinCommand) })
    route("/:pinId")
        .delete(handleRequest {
            endpointHandler(ResponseHelpers.sendDeleteResults("Pin"), ::handleDeletePin)
        })
}

suspend fun SavePinCommandDispatcher.handleSavePinCommand(request: Request) = request.savePinCommand()
    .perform()
    .toJson()

fun Request.savePinCommand() = jsonBody().toPin().let(tribeId()::with).let(::SavePinCommand)

private suspend fun DeletePinCommandDispatcher.handleDeletePin(request: Request) = request.deletePinCommand().perform()

private fun Request.deletePinCommand() = DeletePinCommand(tribeId(), pinId())

