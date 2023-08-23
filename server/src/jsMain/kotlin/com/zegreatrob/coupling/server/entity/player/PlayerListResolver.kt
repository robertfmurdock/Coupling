package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.PlayerPair
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.server.action.player.PairAssignmentHistoryQuery
import com.zegreatrob.coupling.server.action.player.PairCountQuery
import com.zegreatrob.coupling.server.action.player.PairHeatQuery
import com.zegreatrob.coupling.server.action.player.PairsQuery
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.SpinsSinceLastPairedQuery
import com.zegreatrob.coupling.server.action.player.SpinsUntilFullRotationQuery
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

val spinsUntilFullRotationResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> SpinsUntilFullRotationQuery(PartyId(data.id)) },
    fireFunc = ::perform,
    toSerializable = { it },
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

val pairAssignmentHistoryResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: JsonNull -> context.commandDispatcher },
    commandFunc = { data, _ ->
        val model = data.toModel()
        val partyId = PartyId(data.partyId ?: return@dispatch null)
        val players = model.players?.elements ?: return@dispatch null
        PairAssignmentHistoryQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<PairAssignmentDocument>::toSerializable) },
)

val spinsSinceLastPairedResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: JsonNull -> context.commandDispatcher },
    commandFunc = { data, _ ->
        val model = data.toModel()
        val partyId = data.partyId?.let(::PartyId) ?: return@dispatch null
        val players = model.players?.elements ?: return@dispatch null
        SpinsSinceLastPairedQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)

val pairHeatResolve = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: JsonNull -> context.commandDispatcher },
    commandFunc = { data, _ ->
        val model = data.toModel()
        val partyId = data.partyId?.let(::PartyId) ?: return@dispatch null
        val players = model.players?.elements ?: return@dispatch null
        PairHeatQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
