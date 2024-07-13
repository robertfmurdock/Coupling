package com.zegreatrob.coupling.server.action.contribution

import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionGet
import kotlin.time.Duration

interface PartyIdContributionsTrait {
    val contributionRepository: ContributionGet
    suspend fun PartyId.contributions(window: Duration? = null) = contributionRepository.get(this, window)
}
