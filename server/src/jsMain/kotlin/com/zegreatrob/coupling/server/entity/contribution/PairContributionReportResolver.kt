package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.GqlContributionsInput
import com.zegreatrob.coupling.json.GqlPair
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toModel
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.pairassignmentdocument.toCouplingPair
import com.zegreatrob.coupling.server.action.contribution.PairContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch

private val pairContributionQueryFunc = lambda@{ data: GqlPair, input: GqlContributionsInput? ->
    val model = data.toModel()
    val partyId = data.partyId ?: return@lambda null
    val players = model.players?.elements ?: return@lambda null
    PairContributionQuery(
        partyId = partyId,
        pair = players.toCouplingPair(),
        window = input?.window?.toModel(),
        limit = input?.limit,
    )
}

val pairContributionReportResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: GqlPair, _: GqlContributionsInput? -> context.commandDispatcher },
    commandFunc = pairContributionQueryFunc,
    fireFunc = ::perform,
    toSerializable = { it.toJson() },
)
