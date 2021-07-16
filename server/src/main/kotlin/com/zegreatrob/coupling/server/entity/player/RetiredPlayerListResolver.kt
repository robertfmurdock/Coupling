package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.tribeCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonElement

val retiredPlayerListResolve = dispatch(tribeCommand, { _, _: JsonElement -> RetiredPlayersQuery }, ::toJsonArray)

private fun toJsonArray(list: List<TribeRecord<Player>>) = list.map(TribeRecord<Player>::toSerializable)
