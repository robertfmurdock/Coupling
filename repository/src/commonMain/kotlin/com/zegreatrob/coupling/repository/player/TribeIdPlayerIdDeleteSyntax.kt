package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.player.TribeIdPlayerId

interface TribeIdPlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun TribeIdPlayerId.deletePlayer() = playerRepository.deletePlayer(tribeId, playerId)
}
