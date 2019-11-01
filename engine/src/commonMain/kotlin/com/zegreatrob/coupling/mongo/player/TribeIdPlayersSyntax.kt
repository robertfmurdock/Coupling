package com.zegreatrob.coupling.mongo.player

import com.zegreatrob.coupling.model.player.PlayerGetDeleted
import com.zegreatrob.coupling.model.player.PlayerGetter
import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayersAsync(this).await()
}

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetDeleted
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeletedAsync(this).await()
}