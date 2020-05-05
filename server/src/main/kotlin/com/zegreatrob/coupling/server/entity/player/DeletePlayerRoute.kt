package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.server.ResponseHelpers
import com.zegreatrob.coupling.server.action.player.DeletePlayerCommand
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.playerId
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

private val sendDeleteResults = ResponseHelpers.sendDeleteResults("Player")

val deletePlayerRoute = dispatchCommand(::command, { it.perform() }, { it }, sendDeleteResults)

private fun command(request: Request) = with(request) { DeletePlayerCommand(tribeId(), playerId()) }
