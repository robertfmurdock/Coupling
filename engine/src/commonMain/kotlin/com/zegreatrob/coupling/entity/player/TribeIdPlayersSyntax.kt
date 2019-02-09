package com.zegreatrob.coupling.entity.player

import com.zegreatrob.coupling.common.entity.tribe.TribeId

interface TribeIdPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayersAsync(this).await()
}

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeletedAsync(this).await()
}