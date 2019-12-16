package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.repository.player.PlayerRepository
import com.zegreatrob.coupling.model.user.UserEmailSyntax

interface UserPlayersSyntax : UserEmailSyntax {
    val playerRepository: PlayerRepository
    suspend fun getUserPlayers() = playerRepository.getPlayersByEmail(userEmail)
}