package com.zegreatrob.coupling.server.route

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.server.ResponseHelpers.sendDeleteResults
import com.zegreatrob.coupling.server.action.player.*
import com.zegreatrob.coupling.server.external.express.*

val playerRouter = Router(routerParams(mergeParams = true)).apply {
    route("/")
        .post(handleRequest { endpointHandler(Response::sendSuccessful, ::handleSavePlayer) })
    route("/:playerId")
        .delete(handleRequest { endpointHandler(sendDeleteResults("Player"), ::handleDeletePlayer) })
    route("/retired")
        .get(handleRequest { endpointHandler(Response::sendSuccessful, ::handleRetiredPlayerList) })
}

private suspend fun SavePlayerCommandDispatcher.handleSavePlayer(request: Request) = request.savePlayerCommand()
    .perform()
    .toJson()

private fun Request.savePlayerCommand() = SavePlayerCommand(
    tribeId().with(jsonBody().toPlayer())
)

private suspend fun DeletePlayerCommandDispatcher.handleDeletePlayer(request: Request) = request.deletePlayerCommand()
    .perform()

private fun Request.deletePlayerCommand() = DeletePlayerCommand(tribeId(), playerId())

private suspend fun RetiredPlayersQueryDispatcher.handleRetiredPlayerList(request: Request) = request
    .retiredPlayersQuery()
    .perform()
    .toJsonArray()

private fun List<TribeRecord<Player>>.toJsonArray() = map { it.toJson().add(it.data.player.toJson()) }
    .toTypedArray()

private fun Request.retiredPlayersQuery() = RetiredPlayersQuery(tribeId())
