package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.external.express.Request
import com.zegreatrob.coupling.server.external.express.tribeId
import com.zegreatrob.coupling.server.express.route.dispatchCommand

val retiredPlayerListResolve = dispatchCommand(::command, { it.perform() }, ::toJsonArray)

private fun toJsonArray(list: List<TribeRecord<Player>>) = list.map {
    it.toJson().add(it.data.player.toJson())
}.toTypedArray()

private fun command(request: Request) = with(request) { RetiredPlayersQuery(tribeId()) }
