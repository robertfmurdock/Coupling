package com.zegreatrob.coupling.server.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.action.pin.SavePinCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.route.dispatch

val savePinRoute = dispatch { endpointHandler(Response::sendSuccessful, ::handleSavePinCommand) }

suspend fun SavePinCommandDispatcher.handleSavePinCommand(request: Request) = request.savePinCommand()
    .perform()
    .toJson()

fun Request.savePinCommand() = jsonBody().toPin().let(tribeId()::with).let(::SavePinCommand)
