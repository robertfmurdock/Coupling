package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.GqlPair
import com.zegreatrob.coupling.json.GqlPairingSet
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.orderedPairedPlayers
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.defaultPlayer
import com.zegreatrob.coupling.server.action.player.PairAssignmentHistoryQuery
import com.zegreatrob.coupling.server.action.player.PlayersQuery
import com.zegreatrob.coupling.server.action.player.RecentTimesPairedQuery
import com.zegreatrob.coupling.server.action.player.SpinsSinceLastPairedQuery
import com.zegreatrob.coupling.server.action.player.SpinsUntilFullRotationQuery
import com.zegreatrob.coupling.server.action.player.perform
import com.zegreatrob.coupling.server.entity.boost.adapt
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.DispatcherProviders.partyCommand
import com.zegreatrob.coupling.server.graphql.dispatch

val playerListResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> PlayersQuery(data.id) },
    fireFunc = ::perform,
    toSerializable = { it.map(Record<PartyElement<Player>>::toSerializable) },
)

val spinsUntilFullRotationResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> SpinsUntilFullRotationQuery(data.id) },
    fireFunc = ::perform,
    toSerializable = { it },
)

val pairingSetListResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = data.partyId ?: return@dispatch null
        val players = model.players.elements
        PairAssignmentHistoryQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { (pair, history) -> history.map { it.toSerializable() } },
)

val pairAssignmentHeatResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPairingSet, _ ->
        val model = data.toModel()
        val record = model.data
        val partyId = record.partyId
        val pairingSet = record.element
        val pair = pairingSet.orderedPairedPlayers()
            .map { it.id }
            .map { defaultPlayer.copy(id = it) }
            .toCouplingPair()
        RecentTimesPairedQuery(
            partyId = partyId,
            pair = pair,
            lastAssignments = pairingSet.id,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)

val spinsSinceLastPairedResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = data.partyId ?: return@dispatch null
        val players = model.players.elements
        SpinsSinceLastPairedQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)

val pairHeatResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = data.partyId ?: return@dispatch null
        val players = model.players.elements
        RecentTimesPairedQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
            lastAssignments = null,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
