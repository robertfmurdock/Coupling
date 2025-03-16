package com.zegreatrob.coupling.server.action.party

import com.zegreatrob.coupling.model.user.UserIdProvider
import com.zegreatrob.coupling.repository.player.PlayerListGetByEmail

interface UserPlayerIdsSyntax : UserIdProvider {
    val playerRepository: PlayerListGetByEmail
    suspend fun getUserPlayerIds() = playerRepository.getPlayerIdsByEmail(userId.toString())
}
