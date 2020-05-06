package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pinId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.express.route.dispatchCommand

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Pin")

val deletePinRoute = dispatchCommand(::deletePinCommand, { it.perform() }, { it }, sendDeleteResults)

private fun deletePinCommand(request: Request) = with(request) { DeletePinCommand(tribeId(), pinId()) }
