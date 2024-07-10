package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet

interface PartyIdContributionsTrait {
    val contributionRepository: ContributionGet
    suspend fun PartyId.contributions() = contributionRepository.get(this)
}
