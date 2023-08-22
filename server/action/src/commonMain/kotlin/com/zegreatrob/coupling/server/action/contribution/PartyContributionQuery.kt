package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import com.zegreatrob.testmints.action.annotation.ActionMint

@ActionMint
data class PartyContributionQuery(val partyId: PartyId) {
    interface Dispatcher {
        val contributionRepository: ContributionGet
        suspend fun perform(query: PartyContributionQuery): List<PartyRecord<Contribution>> =
            contributionRepository.get(query.partyId)
    }
}
