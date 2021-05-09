package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val retiredPlayerListResolve = dispatch(tribeCommand, { _, _ -> RetiredPlayersQuery }, ::toJsonArray)

private fun toJsonArray(list: List<TribeRecord<Player>>) = list.map {
    it.toJson().add(it.data.player.toJson())
}.toTypedArray()
