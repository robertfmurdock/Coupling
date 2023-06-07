package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val playerListResolve = dispatch(
    dispatcherFunc = partyCommand,
    queryFunc = { _, _ -> PlayersQuery },
    toSerializable = { it?.map(Record<PartyElement<Player>>::toSerializable) },
)
