package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.express.ResponseHelpers
import com.zegreatrob.coupling.server.express.route.ExpressDispatchers.command
import com.zegreatrob.coupling.server.express.route.dispatch
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.playerId
import com.zegreatrob.coupling.server.external.express.tribeId

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Player")

val deletePlayerRoute = dispatch(command, ::deletePlayerCommand, { it }, sendDeleteResults)

private fun deletePlayerCommand(request: Request) = with(request) { DeletePlayerCommand(tribeId(), playerId()) }
