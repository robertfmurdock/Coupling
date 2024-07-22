package com.zegreatrob.coupling.server.repository.cache

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedPlayerRepository(private val repository: PlayerEmailRepository) : PlayerEmailRepository by repository {

    private val mutex = Mutex()
    private val cache = mutableMapOf<PartyId, List<PartyRecord<Player>>>()

    override suspend fun getPlayers(partyId: PartyId) = mutex.withLock {
        cache.getOrPut(partyId) { repository.getPlayers(partyId) }
    }
}
