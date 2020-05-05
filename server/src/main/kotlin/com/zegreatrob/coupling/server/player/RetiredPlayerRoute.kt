package com.zegreatrob.coupling.server.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQueryDispatcher
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.Response
import com.zegreatrob.coupling.server.external.express.sendSuccessful
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.route.dispatchCommand

val retiredPlayerRoute = dispatchCommand { endpointHandler(Response::sendSuccessful, ::handleRetiredPlayerList) }

private suspend fun RetiredPlayersQueryDispatcher.handleRetiredPlayerList(request: Request) = request
    .retiredPlayersQuery()
    .perform()
    .toJsonArray()

private fun List<TribeRecord<Player>>.toJsonArray() = map { it.toJson().add(it.data.player.toJson()) }
    .toTypedArray()

private fun Request.retiredPlayersQuery() = RetiredPlayersQuery(tribeId())
