package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail

interface UserPlayersSyntax : UserEmailSyntax {
    val playerRepository: PlayerListGetByEmail
    suspend fun getUserPlayers() = playerRepository.getPlayersByEmail(userEmail)
}