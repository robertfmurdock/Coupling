package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.UserContextSyntax
import com.zegreatrob.coupling.server.entity.player.PlayerRepository

interface UserPlayersSyntax : UserContextSyntax {
    val playerRepository: PlayerRepository
    fun getUserPlayersAsync() = playerRepository.getPlayersByEmailAsync(userEmail())
}