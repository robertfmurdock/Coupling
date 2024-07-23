package com.zegreatrob.coupling.server.entity.contribution

import com.zegreatrob.coupling.action.stats.halfwayValue
import com.zegreatrob.coupling.json.JsonContributionStatistics
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.Contributor
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.server.action.contribution.PartyContributionQuery
import com.zegreatrob.coupling.server.action.contribution.PartyContributorQuery
import com.zegreatrob.coupling.server.action.contribution.perform
import com.zegreatrob.coupling.server.graphql.DispatcherProviders
import com.zegreatrob.coupling.server.graphql.dispatch
import kotlinx.serialization.json.JsonNull

val contributorResolver = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _: JsonNull? -> data.id?.let(::PartyId)?.let { PartyContributorQuery(it) } },
    fireFunc = ::perform,
    toSerializable = { it.map(PartyElement<Contributor>::toJson) },
)

val partyContributionStatisticsResolver = dispatch(
    dispatcherFunc = DispatcherProviders.partyCommand,
    commandFunc = { data, _: JsonNull? -> data.id?.let(::PartyId)?.let { PartyContributionQuery(it, null, null) } },
    fireFunc = ::perform,
    toSerializable = { contributions ->
        val cycleTimeContributions = contributions.elements.mapNotNull(Contribution::cycleTime)
        JsonContributionStatistics(
            count = contributions.count(),
            medianCycleTime = cycleTimeContributions.sorted().halfwayValue(),
            withCycleTimeCount = cycleTimeContributions.size,
        )
    },
)
