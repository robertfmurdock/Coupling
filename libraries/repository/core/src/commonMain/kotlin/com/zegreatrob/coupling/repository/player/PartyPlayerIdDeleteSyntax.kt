package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.player.PartyIdPlayerId

interface PartyPlayerIdDeleteSyntax {
    val playerRepository: PlayerDelete
    suspend fun PartyIdPlayerId.deletePlayer() = playerRepository.deletePlayer(partyId, playerId)
}
