package com.zegreatrob.coupling.server.repository.cache

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairingSet
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedPairAssignmentDocumentRepository(private val repository: PairAssignmentDocumentRepository) : PairAssignmentDocumentRepository by repository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, List<PartyRecord<PairingSet>>>()

    override suspend fun loadPairAssignments(partyId: PartyId) = mutex.withLock {
        cache.getOrPut(partyId) {
            repository.loadPairAssignments(partyId)
        }
    }
}
