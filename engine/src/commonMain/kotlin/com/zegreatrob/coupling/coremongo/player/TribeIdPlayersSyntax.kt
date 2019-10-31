package com.zegreatrob.coupling.coremongo.player

import com.zegreatrob.coupling.core.entity.tribe.TribeId

interface TribeIdPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayersAsync(this).await()
}

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetDeleted
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeletedAsync(this).await()
}