package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.GqlPairsInput
import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.server.action.player.PairListQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch

val pairsResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: GqlParty, _: GqlPairsInput? -> context.commandDispatcher },
    commandFunc = { data, input: GqlPairsInput? -> PairListQuery(data.id, input?.includeRetired) },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<PlayerPair>::toJson) },
)
