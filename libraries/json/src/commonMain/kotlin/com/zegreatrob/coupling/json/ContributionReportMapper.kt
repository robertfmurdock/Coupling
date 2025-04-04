package com.zegreatrob.coupling.json

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.PartyRecord
import org.kotools.types.ExperimentalKotoolsTypesApi

@OptIn(ExperimentalKotoolsTypesApi::class)
fun GqlContributionReport.toModel() = ContributionReport(
    contributions = contributions?.map(GqlContribution::toModel),
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
    contributors = contributors?.map(GqlContributor::toModel),
    partyId = partyId,
)

fun ContributionReport.toJson() = GqlContributionReport(
    contributions = contributions?.map(PartyRecord<Contribution>::toJson),
    contributors = contributors?.map { it.toJson() } ?: emptyList(),
    count = count,
    medianCycleTime = medianCycleTime,
    withCycleTimeCount = withCycleTimeCount,
    partyId = partyId,
)
