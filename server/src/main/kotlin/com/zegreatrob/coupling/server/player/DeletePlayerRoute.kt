package com.zegreatrob.coupling.server.player

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.playerId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Player")

val deletePlayerRoute = dispatchCommand { endpointHandler(sendDeleteResults, ::handleDeletePlayer) }

private suspend fun DeletePlayerCommandDispatcher.handleDeletePlayer(request: Request) = request
    .deletePlayerCommand()
    .perform()

private fun Request.deletePlayerCommand() = DeletePlayerCommand(tribeId(), playerId())
