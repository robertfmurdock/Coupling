package com.zegreatrob.coupling.server.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.action.player.SavePlayerCommand
import com.zegreatrob.coupling.server.action.player.SavePlayerCommandDispatcher
import com.zegreatrob.coupling.server.external.express.*
import com.zegreatrob.coupling.server.route.dispatch

val savePlayerRoute = dispatch { endpointHandler(Response::sendSuccessful, ::handleSavePlayer) }

private suspend fun SavePlayerCommandDispatcher.handleSavePlayer(request: Request) = request.savePlayerCommand()
    .perform()
    .toJson()

private fun Request.savePlayerCommand() = SavePlayerCommand(tribeId().with(jsonBody().toPlayer()))
