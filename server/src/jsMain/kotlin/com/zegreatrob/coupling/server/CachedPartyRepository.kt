package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.party.PartyRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedPartyRepository(private val repository: PartyRepository) : PartyRepository by repository {

    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, Record<PartyDetails>?>()

    override suspend fun loadParties(partyIds: Set<PartyId>): List<Record<PartyDetails>> = mutex.withLock {
        repository.loadParties(partyIds).also { cache.putAll(it.associateBy { record -> record.data.id }) }
    }

    override suspend fun getDetails(partyId: PartyId): Record<PartyDetails>? = mutex.withLock {
        cache.getOrPut(partyId) { repository.getDetails(partyId) }
    }
}
