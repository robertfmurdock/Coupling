package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class CachedPlayerRepository(private val repository: PlayerEmailRepository) :
    PlayerEmailRepository by repository {

    private val cache = mutableMapOf<PartyId, List<PartyRecord<Player>>>()

    override suspend fun getPlayers(partyId: PartyId) = cache.getOrPut(partyId) {
        repository.getPlayers(partyId)
    }
}

class CachedPairAssignmentDocumentRepository(private val repository: PairAssignmentDocumentRepository) :
    PairAssignmentDocumentRepository by repository {

    private val cache = mutableMapOf<PartyId, List<PartyRecord<PairAssignmentDocument>>>()

    override suspend fun loadPairAssignments(partyId: PartyId) = cache.getOrPut(partyId) {
        repository.loadPairAssignments(partyId)
    }
}
