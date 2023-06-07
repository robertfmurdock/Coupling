package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val retiredPlayerListResolve = dispatch(partyCommand, { _, _ -> RetiredPlayersQuery }, ::toJsonArray)

private fun toJsonArray(list: List<PartyRecord<Player>>?) = list?.map(PartyRecord<Player>::toSerializable)
