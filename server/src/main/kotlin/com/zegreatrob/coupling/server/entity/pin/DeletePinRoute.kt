package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.server.action.pin.DeletePinCommand
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.pinId
import com.zegreatrob.coupling.server.external.express.tribeId

val deletePinRoute = dispatch(command, ::deletePinCommand)

private fun deletePinCommand(request: Request) = with(request) { DeletePinCommand(tribeId(), pinId()) }
