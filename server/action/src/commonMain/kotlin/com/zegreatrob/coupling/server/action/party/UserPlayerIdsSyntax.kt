package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.UserIdSyntax
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail

interface UserPlayerIdsSyntax : UserIdSyntax {
    val playerRepository: PlayerListGetByEmail
    suspend fun getUserPlayerIds() = playerRepository.getPlayerIdsByEmail(userId)
}
