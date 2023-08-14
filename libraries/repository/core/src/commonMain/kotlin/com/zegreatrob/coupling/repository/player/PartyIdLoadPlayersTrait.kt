package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdLoadPlayersTrait {
    val playerRepository: PlayerListGet
    suspend fun PartyId.loadPlayers() = playerRepository.getPlayers(this)
}
