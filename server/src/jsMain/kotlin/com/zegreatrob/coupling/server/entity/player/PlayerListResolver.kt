package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.PairsQuery
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val playerListResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> PlayersQuery(PartyId(data.id)) },
    fireFunc = ::perform,
    toSerializable = { it.map(Record<PartyElement<Player>>::toSerializable) },
)

val pairsResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> PairsQuery(PartyId(data.id)) },
    fireFunc = ::perform,
    toSerializable = { it.map(PlayerPair::toJson) },
)
