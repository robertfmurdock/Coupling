package com.zegreatrob.coupling.server.entity.player

import com.zegreatrob.coupling.json.GqlPair
import com.zegreatrob.coupling.json.GqlPairAssignment
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.json.toSerializable
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.CouplingPair
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignment
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
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
    commandFunc = { data, _ -> data.id.let(::PartyId).let { PlayersQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(Record<PartyElement<Player>>::toSerializable) },
)

val spinsUntilFullRotationResolve = dispatch(
    dispatcherFunc = partyCommand,
    commandFunc = { data, _ -> data.id.let(::PartyId).let { SpinsUntilFullRotationQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it },
)

val pairAssignmentHistoryResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = PartyId(data.partyId ?: return@dispatch null)
        val players = model.players?.elements ?: return@dispatch null
        PairAssignmentHistoryQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
        )
    },
    fireFunc = ::perform,
    toSerializable = { (pair, history) ->
        history.map { doc -> pairAssignment(pair, doc) }.map(PairAssignment::toSerializable)
    },
)

private fun pairAssignment(
    pair: CouplingPair,
    doc: PartyRecord<PairAssignmentDocument>,
) = PairAssignment(
    playerIds = pair.map { it.id },
    details = doc,
    documentId = doc.data.element.id,
    allPairs = doc.data.element.pairs,
    date = doc.data.element.date,
    recentTimesPaired = null,
)

val pairAssignmentHeatResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPairAssignment, _ ->
        val model = data.toModel()
        val partyId = model.details?.data?.partyId ?: return@dispatch null
        val pair = model.playerIds?.map { defaultPlayer.copy(id = it) }?.toCouplingPair() ?: return@dispatch null
        RecentTimesPairedQuery(
            partyId = partyId,
            pair = pair,
            lastAssignments = model.documentId,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)

val spinsSinceLastPairedResolve = dispatch(
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
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
    dispatcherFunc = adapt { context: CouplingContext -> context.commandDispatcher },
    commandFunc = { data: GqlPair, _ ->
        val model = data.toModel()
        val partyId = data.partyId?.let(::PartyId) ?: return@dispatch null
        val players = model.players?.elements ?: return@dispatch null
        RecentTimesPairedQuery(
            partyId = partyId,
            pair = players.toCouplingPair(),
            lastAssignments = null,
        )
    },
    fireFunc = ::perform,
    toSerializable = { it },
)
