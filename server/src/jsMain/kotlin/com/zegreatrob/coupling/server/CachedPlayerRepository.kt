package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.Contribution
import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.contribution.ContributionRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

class CachedPlayerRepository(private val repository: PlayerEmailRepository) : PlayerEmailRepository by repository {

    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, List<PartyRecord<Player>>>()

    override suspend fun getPlayers(partyId: PartyId) = mutex.withLock {
        cache.getOrPut(partyId) { repository.getPlayers(partyId) }
    }
}

class CachedPairAssignmentDocumentRepository(private val repository: PairAssignmentDocumentRepository) : PairAssignmentDocumentRepository by repository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, List<PartyRecord<PairAssignmentDocument>>>()

    override suspend fun loadPairAssignments(partyId: PartyId) = mutex.withLock {
        cache.getOrPut(partyId) {
            repository.loadPairAssignments(partyId)
        }
    }
}

class CachedContributionRepository(private val repository: ContributionRepository) : ContributionRepository by repository {
    private val mutex = Mutex()
    private val cache = mutableMapOf<Pair<PartyId, Duration?>, List<PartyRecord<Contribution>>>()

    override suspend fun get(partyId: PartyId, window: Duration?): List<PartyRecord<Contribution>> = mutex.withLock {
        cache.getOrPut(partyId to window) {
            repository.get(partyId, window)
        }
    }
}
