package com.zegreatrob.coupling.server.entity.tribe

import com.zegreatrob.coupling.UserEmailSyntax
import com.zegreatrob.coupling.mongo.player.PlayerRepository

interface UserPlayersSyntax : UserEmailSyntax {
    val playerRepository: PlayerRepository
    fun getUserPlayersAsync() = playerRepository.getPlayersByEmailAsync(userEmail)
}