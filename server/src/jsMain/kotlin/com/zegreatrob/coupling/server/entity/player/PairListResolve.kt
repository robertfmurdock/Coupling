package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.GqlPairListInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.server.action.player.PairListQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.GqlPartyNode
import com.zegreatrob.coupling.server.graphql.dispatch

val pairListResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: GqlPartyNode, _: GqlPairListInput? -> context.commandDispatcher },
    commandFunc = { data, input: GqlPairListInput? -> PairListQuery(data.id, input?.includeRetired) },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<PlayerPair>::toJson) },
)
