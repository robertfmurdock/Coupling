package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetDeleted
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeleted(this)
}