package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.action.UserEmailSyntax
import com.zegreatrob.coupling.model.player.PlayerRepository

interface UserPlayersSyntax : UserEmailSyntax {
    val playerRepository: PlayerRepository
    fun getUserPlayersAsync() = playerRepository.getPlayersByEmailAsync(userEmail)
}