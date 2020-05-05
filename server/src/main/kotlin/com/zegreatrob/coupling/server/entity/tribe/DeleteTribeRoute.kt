package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

val deleteTribeRoute = dispatchCommand(::deleteTribeCommand, { it.perform() }, { it }, sendDeleteResults("Tribe"))

private fun deleteTribeCommand(request: Request) = with(request) { DeleteTribeCommand(tribeId()) }