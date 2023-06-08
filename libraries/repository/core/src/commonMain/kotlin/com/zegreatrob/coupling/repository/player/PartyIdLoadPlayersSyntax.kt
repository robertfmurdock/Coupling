package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.party.PartyId

interface PartyIdLoadPlayersSyntax {
    val playerRepository: PlayerListGet
    suspend fun PartyId.loadPlayers() = playerRepository.getPlayers(this)
}
