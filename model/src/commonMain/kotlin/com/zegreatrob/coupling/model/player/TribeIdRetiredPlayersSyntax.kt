package com.zegreatrob.coupling.model.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdRetiredPlayersSyntax {
    val playerRepository: PlayerGetDeleted
    suspend fun TribeId.loadRetiredPlayers() = playerRepository.getDeleted(this)
}