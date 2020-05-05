package com.zegreatrob.coupling.server.pin

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.action.pin.DeletePinCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pinId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatch

val deletePinRoute = dispatch { endpointHandler(ResponseHelpers.sendDeleteResults("Pin"), ::handleDeletePin) }

private suspend fun DeletePinCommandDispatcher.handleDeletePin(request: Request) = request.deletePinCommand().perform()

private fun Request.deletePinCommand() = DeletePinCommand(tribeId(), pinId())
