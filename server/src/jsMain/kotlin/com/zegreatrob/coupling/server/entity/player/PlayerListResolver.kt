package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.PairCountQuery
import com.zegreatrob.coupling.server.action.player.PairsQuery
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

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
    toSerializable = { it.map(PartyElement<PlayerPair>::toJson) },
)

val pairCountResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: JsonNull -> context.commandDispatcher },
    commandFunc = { data, _ ->
        val model = data.toModel()
        val partyId = PartyId(data.partyId!!)
        val players = model.players!!.elements
        PairCountQuery(
            partyId = partyId,
            pair = CouplingPair.Double(players[0], players[1]),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
