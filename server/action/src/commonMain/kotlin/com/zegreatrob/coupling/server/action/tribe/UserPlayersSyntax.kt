package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.model.player.PlayerRepository

interface UserPlayersSyntax : UserEmailSyntax {
    val playerRepository: PlayerRepository
    fun getUserPlayersAsync() = playerRepository.getPlayersByEmailAsync(userEmail)
}