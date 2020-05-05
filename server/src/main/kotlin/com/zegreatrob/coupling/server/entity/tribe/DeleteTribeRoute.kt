package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.server.DeleteTribeCommand
import com.zegreatrob.coupling.server.DeleteTribeCommandDispatcher
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

val deleteTribeRoute = dispatchCommand { endpointHandler(sendDeleteResults("Tribe"), ::runCommand) }

private suspend fun DeleteTribeCommandDispatcher.runCommand(request: Request) = request.deleteTribeCommand()
    .perform()

private fun Request.deleteTribeCommand() = DeleteTribeCommand(tribeId())