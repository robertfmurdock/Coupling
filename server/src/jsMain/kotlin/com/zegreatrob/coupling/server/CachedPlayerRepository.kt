package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.model.PartyRecord
import com.zegreatrob.coupling.model.party.PartyId
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.repository.player.PlayerEmailRepository

class CachedPlayerRepository(private val playerRepository: PlayerEmailRepository) :
    PlayerEmailRepository by playerRepository {

    private val playersMap = mutableMapOf<PartyId, List<PartyRecord<Player>>>()

    override suspend fun getPlayers(partyId: PartyId) = playersMap.getOrPut(partyId) {
        playerRepository.getPlayers(partyId)
    }
}
