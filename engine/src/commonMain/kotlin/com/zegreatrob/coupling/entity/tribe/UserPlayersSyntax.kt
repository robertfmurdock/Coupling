package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.UserContextSyntax
import com.zegreatrob.coupling.entity.player.PlayerRepository

interface UserPlayersSyntax : UserContextSyntax {
    val playerRepository: PlayerRepository
    fun getUserPlayersAsync() = playerRepository.getPlayersByEmailAsync(userEmail())
}