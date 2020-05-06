package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.express.route.dispatchCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.commandDispatcher
import com.zegreatrob.coupling.server.external.express.playerId
import com.zegreatrob.coupling.server.external.express.tribeId

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Player")

val deletePlayerRoute = dispatchCommand(::command, ::commandDispatcher, { it }, sendDeleteResults)

private fun command(request: Request) = with(request) { DeletePlayerCommand(tribeId(), playerId()) }
