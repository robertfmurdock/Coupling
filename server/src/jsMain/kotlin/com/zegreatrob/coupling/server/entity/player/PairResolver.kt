package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.JsonParty
import com.zegreatrob.coupling.json.PairInput
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.player.PairCountQuery
import com.zegreatrob.coupling.server.action.player.PairQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.entity.boost.requiredInput
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val pairResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonParty, _: PairInput? -> context.commandDispatcher },
    commandFunc = requiredInput { party, input ->
        PairQuery(
            PartyId(party.id ?: return@requiredInput null),
            input.playerIdList,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it?.let(PartyElement<PlayerPair>::toJson) },
)
val pairCountResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: JsonNull? -> context.commandDispatcher },
    commandFunc = { data, _ ->
        val model = data.toModel()
        val partyId = PartyId(data.partyId ?: return@dispatch null)
        val players = model.players?.elements ?: return@dispatch null
        PairCountQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
