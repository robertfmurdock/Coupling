package com.zegreatrob.coupling.server.player

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommandDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.playerId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatch

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Player")

val deletePlayerRoute = dispatch { endpointHandler(sendDeleteResults, ::handleDeletePlayer) }

private suspend fun DeletePlayerCommandDispatcher.handleDeletePlayer(request: Request) = request
    .deletePlayerCommand()
    .perform()

private fun Request.deletePlayerCommand() = DeletePlayerCommand(tribeId(), playerId())
