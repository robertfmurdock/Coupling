package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.action.stats.halfwayValue
import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.ContributionReport
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.elements
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlin.time.Duration

@ActionMint
data class PartyContributionQuery(val partyId: PartyId, val window: Duration?, val limit: Int?) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): ContributionReport {
            val contributions = contributionRepository.get(
                ContributionQueryParams(
                    partyId = query.partyId,
                    window = query.window,
                    limit = query.limit,
                ),
            )
            return contributionReport(contributions, query.partyId)
        }
    }
}

fun contributionReport(
    contributions: List<PartyRecord<Contribution>>,
    partyId: PartyId,
): ContributionReport {
    val cycleTimeContributions = contributions.elements.mapNotNull(Contribution::cycleTime)
    return ContributionReport(
        partyId = partyId,
        contributions = contributions,
        count = contributions.size,
        medianCycleTime = cycleTimeContributions.sorted().halfwayValue(),
        withCycleTimeCount = cycleTimeContributions.size,
        contributors = null,
    )
}
