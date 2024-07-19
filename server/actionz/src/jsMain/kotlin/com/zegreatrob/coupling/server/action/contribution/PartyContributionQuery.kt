package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.testmints.action.annotation.ActionMint
import kotlin.time.Duration

@ActionMint
data class PartyContributionQuery(val partyId: PartyId, val window: Duration?, val limit: Int?) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): List<PartyRecord<Contribution>> =
            contributionRepository.get(
                ContributionQueryParams(
                    query.partyId,
                    window = query.window,
                    limit = query.limit,
                ),
            )
    }
}
