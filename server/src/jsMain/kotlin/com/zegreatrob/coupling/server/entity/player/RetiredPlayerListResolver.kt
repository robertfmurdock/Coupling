package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.RetiredPlayersQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val retiredPlayerListResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> data.id?.let(::PartyId)?.let { RetiredPlayersQuery(it) } },
    fireFunc = ::perform,
    toSerializable = ::toJsonArray,
)

private fun toJsonArray(list: List<PartyRecord<Player>>?) = list?.map(PartyRecord<Player>::toSerializable)
