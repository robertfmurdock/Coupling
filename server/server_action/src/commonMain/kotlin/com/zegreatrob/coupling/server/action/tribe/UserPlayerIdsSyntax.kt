package com.zegreatrob.coupling.server.action.tribe

import com.zegreatrob.coupling.model.user.UserEmailSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail

interface UserPlayerIdsSyntax : UserEmailSyntax {
    val playerRepository: PlayerListGetByEmail
    suspend fun getUserPlayerIds() = playerRepository.getPlayerIdsByEmail(userEmail)
}