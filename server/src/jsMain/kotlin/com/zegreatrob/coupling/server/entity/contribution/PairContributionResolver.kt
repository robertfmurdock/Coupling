package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.ContributionsInput
import com.zegreatrob.coupling.json.JsonPair
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PairContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch

private val pairContributionQueryFunc = lambda@{ data: JsonPair, input: ContributionsInput? ->
    val model = data.toModel()
    val partyId = data.partyId?.let { PartyId(it) } ?: return@lambda null
    val players = model.players?.elements ?: return@lambda null
    PairContributionQuery(
        partyId = partyId,
        pair = players.toCouplingPair(),
        window = input?.window?.toModel(),
        limit = input?.limit,
    )
}

val pairContributionResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonPair, _: ContributionsInput? -> context.commandDispatcher },
    commandFunc = pairContributionQueryFunc,
    fireFunc = ::perform,
    toSerializable = { it.map(PartyRecord<Contribution>::toJson) },
)

val pairContributionStatisticsResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _, _ -> context.commandDispatcher },
    commandFunc = pairContributionQueryFunc,
    fireFunc = ::perform,
    toSerializable = toSerializableContributionStatistics,
)
