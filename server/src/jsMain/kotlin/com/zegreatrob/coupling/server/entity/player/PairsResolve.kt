package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.server.action.player.PairListQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch

val pairsResolve = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _ -> PairListQuery(data.id) },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<PlayerPair>::toJson) },
)
