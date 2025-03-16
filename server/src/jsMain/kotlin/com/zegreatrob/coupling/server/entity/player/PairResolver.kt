package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.GqlPair
import com.zegreatrob.coupling.json.GqlPairInput
import com.zegreatrob.coupling.json.GqlParty
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.server.action.player.PairCountQuery
import com.zegreatrob.coupling.server.action.player.PairQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.entity.boost.adapt
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch

val pairResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _, _ -> context.commandDispatcher },
    commandFunc = requiredInput { party: GqlParty, input: GqlPairInput ->
        PairQuery(
            party.id,
            input.playerIdList.toSet(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it?.let(PartyElement<PlayerPair>::toJson) },
)
val pairCountResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = data.partyId ?: return@dispatch null
        val players = model.players?.elements ?: return@dispatch null
        PairCountQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
