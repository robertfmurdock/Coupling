package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPlayersSyntax {
    val playerRepository: PlayerGetter
    suspend fun TribeId.loadPlayers() = playerRepository.getPlayers(this)
}