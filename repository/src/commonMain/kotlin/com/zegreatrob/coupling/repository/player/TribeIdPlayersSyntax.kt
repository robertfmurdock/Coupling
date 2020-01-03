package com.zegreatrob.coupling.repository.player

import com.zegreatrob.coupling.model.tribe.TribeId

interface TribeIdPlayersSyntax {
    val playerRepository: PlayerListGet
    suspend fun TribeId.getPlayerList() = playerRepository.getPlayers(this)
}