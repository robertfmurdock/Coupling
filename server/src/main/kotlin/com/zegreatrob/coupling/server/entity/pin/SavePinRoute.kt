package com.zegreatrob.coupling.server.entity.pin

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPin
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.action.pin.SavePinCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.jsonBody
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.express.route.dispatchCommand

val savePinRoute = dispatchCommand(::command, { it.perform() }, Pin::toJson)

private fun command(request: Request) = with(request) { jsonBody().toPin().let(tribeId()::with).let(::SavePinCommand) }
