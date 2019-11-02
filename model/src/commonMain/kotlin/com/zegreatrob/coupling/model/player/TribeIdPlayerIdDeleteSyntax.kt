package com.zegreatrob.coupling.model.player

interface TribeIdPlayerIdDeleteSyntax {
    val playerRepository: PlayerDeleter
    suspend fun TribeIdPlayerId.deletePlayer() = playerRepository.deletePlayer(tribeId, playerId)
}
