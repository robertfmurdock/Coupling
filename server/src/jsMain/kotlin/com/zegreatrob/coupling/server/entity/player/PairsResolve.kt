package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.player.PairListQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch

val pairsResolve = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _ -> data.id?.let(::PartyId)?.let { PairListQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<PlayerPair>::toJson) },
)
