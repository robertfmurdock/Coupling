package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.json.JsonContributionStatistics
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.express.route.CouplingContext
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val contributionStatisticsCountResolver = dispatch(
    dispatcherFunc = { context: CouplingContext, _: JsonContributionStatistics, _: JsonNull? -> context.commandDispatcher },
    commandFunc = { data, _: JsonNull? ->
        data.partyId?.let(::PartyId)?.let { PartyContributionQuery(it, null, null) }
    },
    fireFunc = ::perform,
    toSerializable = { it.size },
)
