package com.zegreatrob.coupling.repository.memory

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyElement
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionRepository

class MemoryContributionRepository : ContributionRepository {
    override suspend fun get(params: ContributionQueryParams): List<PartyRecord<Contribution>> = emptyList()
    override suspend fun save(partyContributions: PartyElement<List<Contribution>>) = throw NotImplementedError()
    override suspend fun deleteAll(partyId: PartyId) = throw NotImplementedError()
}
