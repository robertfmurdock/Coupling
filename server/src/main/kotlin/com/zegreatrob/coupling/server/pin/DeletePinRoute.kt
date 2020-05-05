package com.zegreatrob.coupling.server.pin

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pinId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Pin")

val deletePinRoute = dispatchCommand(::deletePinCommand, { it.perform() }, { it }, sendDeleteResults)

private fun deletePinCommand(request: Request) = with(request) { DeletePinCommand(tribeId(), pinId()) }
