package com.zegreatrob.coupling.server.repository.cache

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.ContributionQueryParams
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

class CachedContributionRepository(private val repository: ContributionRepository) : ContributionRepository by repository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<Pair<PartyId, Pair<Duration?, Int?>>, List<PartyRecord<Contribution>>>()

    override suspend fun get(params: ContributionQueryParams): List<PartyRecord<Contribution>> = mutex.withLock {
        val (partyId, window, limit) = params
        cache.getOrPut(partyId to (window to limit)) {
            repository.get(ContributionQueryParams(partyId, window, limit))
        }
    }
}
